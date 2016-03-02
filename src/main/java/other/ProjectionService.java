package other;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.projection.ProjectionFactory;


import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by dave on 12/4/15.
 */
public class ProjectionService {

    @Inject
    private ProjectionFactory factory;

    public <P, E> Page<P> convert(Page<E> src, Class<P> projection){
        List<P> projections = src.getContent().stream()
                .map(e -> e == null ? null : factory.createProjection(projection, e))
                .collect(Collectors.toList());

        Pageable pageable =  new PageRequest(src.getNumber(), src.getSize());
        return new PageImpl<>(projections, pageable, src.getTotalElements());
    }

    public <P, E> P convert(E src, Class<P> projection){
        return factory.createProjection(projection, src);
    }

}
