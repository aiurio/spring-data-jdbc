package io.aiur.oss.db.jdbc.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dave on 12/30/15.
 */
@Slf4j
public class CustomResourceProcessor<T> implements ResourceProcessor<Resource<T>> {

    @Autowired(required = false)
    private HttpServletRequest request;

    @Override
    public Resource<T> process(Resource<T> resource) {
        resource.removeLinks();
        if( request == null ) {
            log.warn("Could not create 'self' link for {} ",
                    resource.getContent() == null ? "null-resource" : resource.getContent().getClass() );
        }else{
            String url = request.getRequestURL().toString() + "?" + request.getQueryString();
            resource.add(new Link(url, Link.REL_SELF));
        }

        return resource;
    }

}