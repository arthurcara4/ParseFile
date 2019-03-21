package br.com.accesstage.parsefile.utils;

import br.com.accesstage.parsefile.utils.Valida;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Email { 
    public static void send(String host, String from, String to, String subject, String body) throws Exception {  
        if (Valida.isEmpty(to)) to = "";
        send(host, from, to, "", subject, body); 
    }
    
    public static void send(String host, String from, String[] to, String subject, String body) throws Exception {  
        send(host, from, to, "".split(","), subject, body); 
    }
    
    public static void send(String host, String from, String to, String bcc, String subject, String body) throws Exception {  
        if (Valida.isEmpty(to)) to = "";
        if (Valida.isEmpty(bcc)) bcc = "";
        send(host, from, to.split(","), bcc.split(","), subject, body); 
    }
        
    public static void send(String host, String from, String[] to, String[] bcc, String subject, String body) throws Exception {  
        
        Properties props = System.getProperties();  
        
        props.put("mail.smtp.host",host);  
        Session session = Session.getDefaultInstance(props, null);  
        
        Message msg = new MimeMessage(session);  
        
        msg.setFrom(new InternetAddress(from));  
        
        ArrayList toArr = new ArrayList();
        for (int i = 0; i < to.length; i++) {
            if (!Valida.isEmpty(to[i])) 
                toArr.add(new InternetAddress(to[i].trim()));
        }
        if (!Valida.isEmpty(toArr)) {
            msg.setRecipients(Message.RecipientType.TO, (InternetAddress[]) toArr.toArray(new InternetAddress[0]));
        }
          
        ArrayList bccArr = new ArrayList();
        for (int i = 0; i < bcc.length; i++) {
            if (!Valida.isEmpty(bcc[i])) 
                bccArr.add(new InternetAddress(bcc[i].trim()));
                
        }
        if (!Valida.isEmpty(bccArr)) 
            msg.setRecipients(Message.RecipientType.BCC, (InternetAddress[])bccArr.toArray(new InternetAddress[0]));  
        
        msg.setSubject(subject);  
        
        // cria a primeira parte da mensagem
        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setContent(body, "text/html");
        
        // Cria o Multipart a adiciona as duas partes a ele
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(mbp1);
        
        msg.setContent(mp);

        
        Transport.send(msg);  
    }
    
    public static void send(String host, String from, String to, String subject, String body, File anexo) throws Exception {  
        if (Valida.isEmpty(to)) to = "";
        send(host, from, to.split(","), subject, body, new File[] {anexo});
    }
    
    public static void send(String host, String from, String to, String subject, String body, File[] anexo) throws Exception {  
        if (Valida.isEmpty(to)) to = "";
        send(host, from, to.split(","), subject, body, anexo);
    }
        
    public static void send(String host, String from, String[] to, String subject, String body, File anexo) throws Exception {  
        send(host, from, to, subject, body, new File[] {anexo});
    }
    
    public static void send(String host, String from, String[] to, String subject, String body, File[] anexo) throws Exception {  
        
        Properties props = System.getProperties();  
        
        props.put("mail.smtp.host",host);  
        Session session = Session.getDefaultInstance(props, null);  
        
        Message msg = new MimeMessage(session);  
        
        msg.setFrom(new InternetAddress(from));  
        
        InternetAddress[] toArr = new InternetAddress[to.length];
        for (int i = 0; i < to.length; i++) toArr[i] = new InternetAddress(to[i]);
        
        msg.setRecipients(Message.RecipientType.TO, toArr);  
        
        msg.setSubject(subject);  
        
        // Cria o Multipart a adiciona as duas partes a ele
        Multipart mp = new MimeMultipart();
        
        
        // cria a primeira parte da mensagem
        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setContent(body, "text/html");
        
        mp.addBodyPart(mbp1);
        
        if (anexo != null) {
            for (int f = 0; f < anexo.length; f++) {
                // cria a segunda parte da mensagem
                MimeBodyPart mbp2 = new MimeBodyPart();
                
                // anexa o arquivo à mensagem
                FileDataSource fds = new FileDataSource(anexo[f]);
                mbp2.setDataHandler(new DataHandler(fds));
                mbp2.setFileName(fds.getName());
                
                mp.addBodyPart(mbp2);
            }
        }
        
        msg.setContent(mp);

        
        Transport.send(msg);  
    }  
    
} 
