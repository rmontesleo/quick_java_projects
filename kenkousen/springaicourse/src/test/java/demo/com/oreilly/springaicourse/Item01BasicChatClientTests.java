package demo.com.oreilly.springaicourse;


import  static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;

import reactor.core.publisher.Flux;

@SpringBootTest
class Item01BasicChatClientTests {


	record ActorFilms(String actor, List<String> movies) {}

	@Value("classpath:movie_prompt.st")
	private Resource promptTemplate;

	@Autowired
	private OpenAiChatModel model;

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

}
