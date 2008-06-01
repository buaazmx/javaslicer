package de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;

public class GZipIntegerTraceSequence extends IntegerTraceSequence {

    private boolean started = false;
    private boolean ready = false;

    private int lastValue;

    private ByteArrayOutputStream rawOut;
    private GZIPOutputStream zipOut;
    private DataOutputStream dataOut;

    public GZipIntegerTraceSequence(final int index) {
        super(index);
    }

    @Override
    public void trace(final int value) {
        int diffVal = value;
        if (!this.started) {
            this.rawOut = new ByteArrayOutputStream();
            try {
                this.zipOut = new GZIPOutputStream(this.rawOut);
            } catch (final IOException e) {
                // should never occur
                throw new RuntimeException(e);
            }
            this.dataOut = new DataOutputStream(this.zipOut);
            this.started = true;
        } else if (this.ready) {
            throw new RuntimeException("Trace cannot be extended any more");
        } else {
            diffVal -= this.lastValue;
        }
        this.lastValue = value;

        try {
            this.dataOut.writeInt(diffVal);
        } catch (final IOException e) {
            // should never occur
            throw new RuntimeException(e);
        }
    }

    public void writeOut(final ObjectOutputStream out) throws IOException {
        final byte[] data;
        if (!this.started) {
            data = new byte[0];
        } else {
            if (!this.ready) {
                this.dataOut.close();
                this.ready = true;
            }
            data = this.rawOut.toByteArray();
        }
        out.writeByte(TraceSequence.FORMAT_GZIP);
        out.writeByte(TraceSequence.TYPE_INTEGER);
        out.writeInt(data.length);
        out.write(data);
        if (!this.ready) {
            this.zipOut.close();
            this.ready = true;
        }
    }

}
