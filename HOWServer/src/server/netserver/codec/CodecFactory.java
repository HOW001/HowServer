package server.netserver.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class CodecFactory implements ProtocolCodecFactory {
    private ProtocolEncoder encoder;

    private ProtocolDecoder decoder;

    public CodecFactory(ProtocolDecoder decoder,ProtocolEncoder encoder) throws Exception {
        this.decoder = decoder;
        this.encoder = encoder;
    }

    public ProtocolDecoder getDecoder (IoSession session) throws Exception {
        return decoder;
    }

    public ProtocolEncoder getEncoder (IoSession session) throws Exception {
        return encoder;
    }

}
