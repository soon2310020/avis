package saleson.service.s3storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class DoLogService {
    @Autowired
    ThreadPoolTaskExecutor taskExecutor;

    @Value("${log.file.path}")
    String pathFile;
    public void doLog(String header, String content)
    {
        doLogGlobal(header, content, pathFile);
    }
    public void doLogGlobal(final String header, final String content, final String thePath)
    {
        taskExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    writeToFile(header, content, thePath);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

    }

    public void writeToFile(String header, String content, String thePath) throws IOException
    {
        StringBuilder stringBuilder = new StringBuilder();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-M-dd hh:mm:ss");
        String dateToString = simpleDateFormat.format(new Date());
        stringBuilder.append("\n****************************************\n" + dateToString + "\n");
        stringBuilder.append(header + "\n");
        stringBuilder.append(content + "\n");
        java.io.File file = new java.io.File(thePath);
        if (!file.exists())
        {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file, true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(stringBuilder.toString());
        bw.close();
    }

}
