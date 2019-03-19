package pt.ist.sec.g27.hds_notary.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FilterServletOutputStream extends ServletOutputStream {

    private final DataOutputStream dataOutputStream;

    public FilterServletOutputStream(OutputStream outputStream) {
        this.dataOutputStream = new DataOutputStream(outputStream);
    }

    @Override
    public void write(byte[] b) throws IOException {
        dataOutputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        dataOutputStream.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        dataOutputStream.write(b);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener listener) {

    }
}
