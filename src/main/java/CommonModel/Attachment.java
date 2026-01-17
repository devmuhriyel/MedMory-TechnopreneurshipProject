package CommonModel;

import java.io.File;

public class Attachment {

    private final File file;

    public Attachment(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return file.getName();
    }
}
