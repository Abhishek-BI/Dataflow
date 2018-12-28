package com.google.main;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.FileBasedSink;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.fs.ResourceId;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.ValueProvider.NestedValueProvider;
import org.apache.beam.sdk.options.ValueProvider.StaticValueProvider;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.common.collect.Lists;
import com.google.datastore.model.User;

public class MainExecuter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(MainExecuter.class);
	private static String HEADERS = "ID,Code,Value,Date";

	public static DatastoreOptions options1 = DatastoreOptions.newBuilder()
			.setProjectId("virgin-red-test2").build();
	public static Datastore datastore = options1.getService();

	public static void main(String[] args) {
		MainExecuter.executeWithOptions(args);
	}

	public static void executeWithOptions(String[] option) {

		DataflowPipelineOptions options = PipelineOptionsFactory.as(DataflowPipelineOptions.class);
		options.setJobName("FullDataCSV1");
		options.setProject("virgin-red-test2");
		options.setStagingLocation("gs://virgin-red-test2/ETL/staging");
		options.setTempLocation("gs://virgin-red-test2/ETL/tmp");
		options.setRunner(DataflowRunner.class);

		Pipeline p = Pipeline.create(options);

		String input = "gs://virgin-red-test2/ETL/csvInput/emailInput1.csv";
		String outputPrefix = "gs://virgin-red-test2/ETL/PubSubOutput";

		Pipeline readPipeline = p.apply(PubsubIO.readStrings().fromTopic("projects/virgin-red-test2/topics/emailToUserIdExport"))
				.apply("window",
						Window.into(SlidingWindows//
								.of(Duration.standardSeconds(30))//
								.every(Duration.standardSeconds(30)))) //
				.apply("WordsPerLine", ParDo.of(new DoFn<String, String>() {
					@ProcessElement
					public void processElement(ProcessContext c) throws Exception {
						String s = c.element();
						final String start = "Start";
						if (start.equals(s)) {
							c.output(s);
						} else {
							LOG.info("Start not found");
							throw new Exception();
						}
					}
				})).getPipeline();

		PCollection<String> lines = readPipeline.apply("Read File",TextIO.read().from(input));
		PCollection<HashMap<String, List<Entity>>> entitySet = lines.apply("Query", ParDo.of(new DoFn<String, HashMap<String, List<Entity>>>() {
			@ProcessElement
			public void processElement(ProcessContext c) throws Exception {
				String email = c.element();
				HashMap<String, List<Entity>> map =  new HashMap<>();
				Query<Entity> query = Query.newEntityQueryBuilder()
						.setKind("VirginRedUser")
						.setFilter(CompositeFilter.and(
								PropertyFilter.eq("email", email)))
						.build();
				Iterator<Entity> result = datastore.run(query);
				List<Entity> myList = null;
				if (result != null) {
					myList = Lists.newArrayList(result);
					map.put(email, myList);
				} 
				if (myList != null && myList.isEmpty()){
					String encryptedEmail = encryptData(email);
					Query<Entity> query1 = Query.newEntityQueryBuilder()
							.setKind("VirginRedUser")
							.setFilter(CompositeFilter.and(
									PropertyFilter.eq("email", encryptedEmail)))
							.build();
					Iterator<Entity> result1 = datastore.run(query1);
					if (result1 != null ) {
						myList = Lists.newArrayList(result1);
						map.put(email, myList);
					}
				}
				if (map != null) {
					c.output(map);
				}
			}
		}));

		LOG.info("Result Set"+entitySet);
		PCollection<User> userSet = entitySet.apply("Result Set", ParDo.of(new DoFn<HashMap<String, List<Entity>>, User>() {
			@ProcessElement
			public void processElement(ProcessContext c) throws Exception {
				HashMap<String, List<Entity>> resultMap = c.element();
				if (resultMap != null && !resultMap.isEmpty()) {
					Set<String> keySet = resultMap.keySet();
					keySet.forEach(key -> {
						List<Entity> entityList = resultMap.get(key);
						entityList.forEach(entity -> {
							String email = key;
							String encryptedEmail =  entity.getString("email");
							Long userId =  entity.getKey().getId();
							User user = new User(email, encryptedEmail, userId);
							c.output(user);
						});
					});
				}
			}
		}));
		LOG.info("Entity"+userSet);
		PCollection<String> output = userSet.apply("Print Entity", ParDo.of(new DoFn<User, String>() {
			@ProcessElement
			public void processElement(ProcessContext c) throws Exception {
				User user = c.element();
				if (user != null && user.getEmail() != null && user.getEmail().equals(user.getEncryptedEmail())) {
					user.setEncryptedEmail(null);
				}
				c.output(user.toString());
			}
		}));
		output.apply(TextIO.write().withHeader("User Id,Email,Encrypted Email").to(outputPrefix).withSuffix(".csv").withoutSharding());
		readPipeline.run();

	}

	public static String encryptData(String plainText) {
		final Charset UTF_8 = StandardCharsets.UTF_8;
		final byte[] key = { 0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65, 0x74, 0x4b, 0x65, 0x79 };
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			final SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			final String encryptedString = new String(Base64.encodeBase64(cipher.doFinal(plainText.getBytes())), UTF_8);
			return encryptedString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}