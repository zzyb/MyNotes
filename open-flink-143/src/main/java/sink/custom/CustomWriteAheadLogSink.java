package sink.custom;

import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.runtime.operators.CheckpointCommitter;
import org.apache.flink.streaming.runtime.operators.GenericWriteAheadSink;

/**
 *
 */
public class CustomWriteAheadLogSink extends GenericWriteAheadSink<Tuple2<String, String>> {

    public CustomWriteAheadLogSink(CheckpointCommitter committer, TypeSerializer<Tuple2<String, String>> serializer, String jobID) throws Exception {
        super(committer, serializer, jobID);
    }

    @Override
    protected boolean sendValues(Iterable<Tuple2<String, String>> values, long checkpointId, long timestamp) throws Exception {
        return false;
    }
}
