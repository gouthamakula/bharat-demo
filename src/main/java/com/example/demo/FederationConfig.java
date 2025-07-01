package com.example.demo;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
class StaticResourceConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/software/udl/graphql/sg1/graphiql/**")
				.addResourceLocations("classpath:/graphiql/");
	}
}


@Controller
class StaticResourceController{

	@GetMapping("/software/udl/graphql/sg1/graphiql")
	public String forwardToGraphiql() {
		return "forward:/software/udl/graphql/sg1/graphiql/graphiql.html";
	}

}