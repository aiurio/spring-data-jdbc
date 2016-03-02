package io.aiur.oss.db.jdbc.jdbc.mapping;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Maps;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.annotation.PostConstruct;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by dave on 12/16/15.
 */
public class SqlCache {

    private final Map<String, String> map = Maps.newHashMap();

    public String getByKey(String key){
        return map.get(key);
    }

    @PostConstruct
    public void init() throws IOException, XMLStreamException {
        ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        Resource [] resources = patternResolver.getResources("classpath*:**/*.sql.xml");
        for(Resource resource : resources){
            InputStream stream = resource.getInputStream();
            XMLStreamReader sr = XMLInputFactory.newInstance().createXMLStreamReader(stream);
            XmlMapper mapper = new XmlMapper();
            Map<String, String> converted = mapper.readValue(sr, Map.class);
            map.putAll(converted);
        }
    }
}
