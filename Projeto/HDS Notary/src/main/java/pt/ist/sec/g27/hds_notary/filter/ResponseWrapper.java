package pt.ist.sec.g27.hds_notary.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class ResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream byteArrayOutputStream;
    private final FilterServletOutputStream filterServletOutputStream;

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
        byteArrayOutputStream = new ByteArrayOutputStream();
        filterServletOutputStream = new FilterServletOutputStream(byteArrayOutputStream);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return super.getWriter();
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return filterServletOutputStream;
    }

    public byte[] getDataStream() {
        return byteArrayOutputStream.toByteArray();
    }
}
