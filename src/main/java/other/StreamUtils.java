package other;


import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by dave on 9/2/15.
 */
public class StreamUtils {

    public static <T> Stream<T> stream(Iterator<T> itr){
        return stream(() -> itr);
    }

    public static <T> Stream<T> stream(Iterable<T> iterable){
        Stream<T> targetStream = StreamSupport.stream(iterable.spliterator(), false);
        return targetStream;
    }


}
