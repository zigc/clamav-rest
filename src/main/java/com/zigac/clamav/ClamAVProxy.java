package com.zigac.clamav;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fi.solita.clamav.ClamAVClient;

@RestController
public class ClamAVProxy
{
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Value("${clamd.host}")
    private String hostname;

    @Value("${clamd.port}")
    private int port;

    @Value("${clamd.timeout}")
    private int timeout;

    /**
     * @return Clamd status.
     */
    @GetMapping("/ping")
    public String ping() throws IOException
    {
        ClamAVClient a = new ClamAVClient(hostname, port, timeout);

        return "Clamd responding: " + a.ping() + "\n";
    }

    /**
     * @return Clamd scan result
     */
    @PostMapping(value = "/scan")
    public @ResponseBody
    Boolean handleFileUpload(
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file
    ) throws IOException
    {
        if (!file.isEmpty())
        {
            log.info(String.format("Scanning file... %s, %s, %s", name, file.getName(), file.getOriginalFilename()));
            ClamAVClient a = new ClamAVClient(hostname, port, timeout);
            byte[] r = a.scan(file.getInputStream());
            final boolean cleanReply = ClamAVClient.isCleanReply(r);
            if (cleanReply)
            {
                log.info(String.format("File clean: %s, %s, %s", name, file.getName(), file.getOriginalFilename()));
            }
            else
            {
                log.warn(String.format("VIRUS DETECTED for file: %s, %s, %s", name, file.getName(), file.getOriginalFilename()));
            }
            return cleanReply;
        }
        else
        {
            log.error(String.format("Empty file: %s", name));
            throw new IllegalArgumentException("empty file");
        }
    }

    /**
     * @return Clamd scan reply
     */
    @PostMapping(value = "/scanReply")
    public @ResponseBody
    String handleFileUploadReply(
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file
    ) throws IOException
    {
        if (!file.isEmpty())
        {
            log.info(String.format("Scanning file: %s, %s, %s", name, file.getName(), file.getOriginalFilename()));
            ClamAVClient a = new ClamAVClient(hostname, port, timeout);
            return new String(a.scan(file.getInputStream()));
        }
        else
        {
            log.error(String.format("Empty file: %s", name));
            throw new IllegalArgumentException("empty file");
        }
    }
}
