package io;

import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;

@OpenAPIDefinition(
		info = @Info(
					title = "API Quarkus Social - Curso Udemy Prof Douglas - 2022",
					version = "0.0.1",
					contact = @Contact(
								name = "Sebastião Fidêncio da Silva Pereira",
								url = "https://github.com/sfidencio/curso-douglas-udemy-quarkus-social",
								email = "sfidencio@gmail.com"
							),
					license = @License(
								name = "Apache 2.0",
								url = "https://www.apache.org/licenses/LICENSE-2.0.html"
							)
				)
)
public class QuarkusSocial extends Application {

}
