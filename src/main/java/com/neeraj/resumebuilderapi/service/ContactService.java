package com.neeraj.resumebuilderapi.service;

import com.neeraj.resumebuilderapi.document.Contact;
import com.neeraj.resumebuilderapi.repository.ContactRepo;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepo contactRepo;
    private final EmailService emailService;

    public Contact saveMessageAndSendEmail(Contact contact) throws MessagingException {
        // 1. Save data to MongoDB
        Contact savedContact = contactRepo.save(contact);

        // 2. Send Auto-Reply Email to User (Optional: Uncomment if EmailService is ready)

        String emailBody = "Hi " + contact.getFullName() + ",\n\n" +
                "Thank you for contacting CVPie! We have received your message regarding '" +
                contact.getSubject() + "'.\n\nOur team will get back to you shortly.\n\nBest Regards,\nTeam CVPie";

        emailService.sendSimpleEmail(contact.getEmail(), "Thank you for contacting CVPie!", emailBody);


        return savedContact;
    }
}
