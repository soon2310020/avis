package vn.com.twendie.avis.api.rest.config;

import org.springframework.context.annotation.Import;
import vn.com.twendie.avis.api.rest.controller.ErrorController;

@Import(ErrorController.class)
public class GlobalExceptionHandling {

}
