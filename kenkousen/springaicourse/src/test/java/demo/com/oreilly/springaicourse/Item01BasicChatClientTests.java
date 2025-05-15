package demo.com.oreilly.springaicourse;


import  static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;

import reactor.core.publisher.Flux;

@SpringBootTest
class Item01BasicChatClientTests {


	record ActorFilms(String actor, List<String> movies) {}

	@Value("classpath:movie_prompt.st")
	private Resource promptTemplate;

	@Autowired
	private OpenAiChatModel model;

	@Autowired
	private ChatMemory memory;

	@Test
	void contextLoads() {
	}

	@Test
	void simplePrompt() {
		ChatClient chatClient = ChatClient.builder(model).build();
		String response = chatClient.prompt("Telll me a joke")
				.call()
				.content();
		System.out.println(response);
	}

	@Test
	void simplePromptWithAdvisonLoggin() {
		ChatClient chatClient = ChatClient.builder(model).build();
		String response = chatClient
				.prompt()
				.advisors(new SimpleLoggerAdvisor())
				.user("Why is the sky blue?")
				.call()
				.content();
		System.out.println(response);
	}

	@Test
	void simplePromptResponseLikeAPirate() {
		ChatClient chatClient = ChatClient.builder(model).build();
		String response = chatClient.prompt("Telll me a joke")
				.system("You are a helpul assistant that responds like a pirate.")
				.call()
				.content();
		System.out.println(response);
	}

	@Test
	void simpleUserPrompt() {
		ChatClient chatClient = ChatClient.builder(model).build();
		String response = chatClient
				.prompt()
				.user("Why is the sky blue?")
				.call()
				.content();

		System.out.println(response);

	}

	@Test
	void simpleUserPromptWithChatResponse() {
		ChatClient chatClient = ChatClient.builder(model).build();
		ChatResponse response = chatClient
				.prompt()
				.user("Why the ocean is blue")
				.call()
				.chatResponse();

		System.out.println(response);
	}


	/**
	 * Right now (May 2025), the default model was gpt-4o-mini-2024-07-18
	 * TODO: Add the optional to handle responses
	 */
	@Test
	void simpleChatOptionsResponses(){
		ChatClient chatClient = ChatClient.builder(model).build();
		ChatResponse response = chatClient
			.prompt()
			.user("Get a simple and concrete explanaition what is an Embeding in Gen AI")
			.call()
			.chatResponse();			

		assertNotNull(response);
		System.out.println("Model is: " + response.getMetadata().getModel() );
		System.out.println("Usage: " + response.getMetadata().getUsage() );
		System.out.println("Text: " + response.getResult().getOutput().getText() );
		System.out.println("Output: " + response.getResult().getOutput() );
		System.out.println("Result: " + response.getResult() );

		//Optional<ChatResponse> optionalResponse = Optional.of(response);
		//optionalResponse	
	}


	/**
	 * 
	 */
	@Test
	void actorFilmsDemoTest(){
		ChatClient chatClient = ChatClient.builder(model).build();
		ActorFilms actorFilms = chatClient
				.prompt()
				.user("Generate the filmography for a random actor.")
				.call()
				.entity(ActorFilms.class);

		
		assertNotNull(actorFilms);
		System.out.println("Actor: " + actorFilms.actor );
		actorFilms.movies.forEach(System.out::println);
	}


	@Test
	void listOfActorTest(){
		ChatClient chatClient = ChatClient.builder(model).build();
		List<ActorFilms> actorFilmsList = chatClient
				.prompt()
				.user("Generate the filmography of 5 movies for Tom Hanks and Bill Murray.")
				.call()
				.entity( new ParameterizedTypeReference<>() {} );

		assertNotNull(actorFilmsList);

		actorFilmsList.forEach( actorFilms ->{
			System.out.println("Actor: " + actorFilms.actor);
			actorFilms.movies.forEach(System.out::println);
		});
				
	}

	@Test
	void fluxOfStringTest(){
		ChatClient chatClient = ChatClient.builder(model).build();
		Flux<String> output =  chatClient
					.prompt()
					.user("Tell me a joke")
					.stream()
					.content();

		output
			.doOnNext(System.out::println)
			.doOnCancel(()-> System.out.println("Cancelled") )
			.doOnComplete( ()-> System.out.println("Completed") )
			.doOnError( (e)-> System.out.println("Error is: " + e.getMessage() ) )
			.blockLast();
	}



	@Test
	void promptTemplateTest(){
		String answer = ChatClient
				.create(model)
				.prompt()
				.user( 
					u -> u
					.text("Tell me the names of 5 movies whose soundtrack was composed by {composer}")
					.param("composer", "John Williams")  
				)
				.call()
				.content();

		System.out.println("Answer: "  + answer );		
				
	}


	@Test
	void promptTemplateFromResourceTest(){
		String answer = ChatClient
				.create(model)
				.prompt()
				.user( 
					u -> u
					.text(promptTemplate)
					.param("number", 3)
					.param("composer", "Michel Giacchino")  
				)
				.call()
				.content();

		System.out.println("Answer: "  + answer );
	}

	@Test
	void requestAreStatelessTest () {
		ChatClient chatClient = ChatClient.create(model);
		String answer1 = chatClient
				.prompt()
				.user( u -> u
				  		.text("My name is Inigo Montoya. You killed my father. Prepare to die."))
				.call()
				.content();
		System.out.println("answer1: " +answer1);
		
		
		String answer2 = chatClient
				.prompt()
				.user( u -> u
				  		.text("Who am I?"))
				.call()
				.content();
		System.out.println("answer 2: " + answer2);

	}


	@Test
	void requestWithMemoryTest () {
		ChatClient chatClient = ChatClient
				.builder(model)
				.defaultAdvisors( new MessageChatMemoryAdvisor(memory) )
				.build();

		System.out.println("Initial query...");
		String answer1 = chatClient
				.prompt()				
				.user( u -> u
				  		.text("My name is Inigo Montoya. You killed my father. Prepare to die."))
				.call()
				.content();
		System.out.println("answer1: " +answer1);
		

		System.out.println("Second query...");
		String answer2 = chatClient
				.prompt()
				.advisors( new SimpleLoggerAdvisor() )
				.user( u -> u
				  		.text("Who am I?"))
				.call()
				.content();
		System.out.println("answer 2: " + answer2);

	}

	//TODO: Replicate the test that see the terminator image.  Create the test that read teh terminator image
	// From Ken Kousen samples in GitHub

	@Test
	void localVisionTest(){
		String response = ChatClient
			.create(model)
			.prompt()
			.user(u -> u.text("Explain what do you see on this picture")
					.media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource("/items.jpg"))
			)
			.call()
			.content();

		System.out.println(response);



	}


	@Test
	void remoteVisionTest()throws Exception {
		//Ice Age characters
		//String url = "https://th.bing.com/th/id/OIP.IzMevCa-pdUG0gEiC5MCbQHaDt?rs=1&pid=ImgDetMain";

		// Thunderbolts
		//String url = "https://api.time.com/wp-content/uploads/2025/04/david-harbour-hannah-john-kamen-sebastian-stan-florence-pugh-wyatt-russell-thunderbolts.jpg?quality=85&w=1200&h=628&crop=1";

		// Avenger
		String url = "https://static1.moviewebimages.com/wordpress/wp-content/uploads/2022/10/avengers-endgame-big-three-vs-thanos.jpeg";

		String response = ChatClient
			.create(model)
			.prompt()
			.user(u -> {
				try {
					u.text("Explain what do you see on this picture")
							.media(MimeTypeUtils.IMAGE_JPEG, URI.create(url).toURL()  );
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException();
				}
			})
			.call()
			.content();

		System.out.println(response);

	}


	/**
	 * Look into the web debug console to get the imge URL from the generated image
	 * @param imageModel
	 */
	@Test
	void imageGenerator(@Autowired OpenAiImageModel imageModel){
		String prompt = """
				A warrior cat rides a squirrel into battle
				""";
		var imagePrompt = new ImagePrompt(prompt);
		ImageResponse imageResponse = imageModel.call(imagePrompt);
		System.out.println(imageResponse );
	}


	/**
	 * Verify if this method, understand more about @Tools in spring, if they invoke other kind of methods (rest, database, files, etc)
	 * Verify more samples and how to take advantage about @Tools
	 */
	@Test
	void useDateTimeTool(){
		String response = ChatClient
					.create(model)
					.prompt("What day is tomorroy?")
					.tools(new DateTimeTools() )
					.call()
					.content();

		System.out.println("#### Response is " + response);



		String alarmTime = ChatClient
				.create(model)
				.prompt("Set an alarm for ten minuts from now")
				.tools(new DateTimeTools())
				.call()
				.content();

		System.out.println("Alarm set to " + alarmTime);
	}


	//TODO: Replicate the sample TeslaManualServiceTest from Ken Kousen sample in GitHub
	// The sample uses a in memory vector store to learn about RAG


	//TODO: Evaluate Chroma (Vector Database)

	//TODO: Check how to execute the MCP sample from Ken Kousen Claude Test



}
