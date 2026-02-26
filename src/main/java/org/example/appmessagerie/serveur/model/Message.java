package org.example.appmessagerie.serveur.model;
import org.example.appmessagerie.serveur.model.enums.MessageStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, length = 1000)
    private String content;

    private LocalDateTime dateEnvoi;

    @Enumerated(EnumType.STRING)
    private MessageStatus statut;

    public Message() {}

    public Message(User sender, User receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.dateEnvoi = LocalDateTime.now();
        this.statut = MessageStatus.ENVOYE;
    }



    public MessageStatus getStatut() { return statut; }

    public void setStatut(MessageStatus statut) {
        this.statut = statut;
    }
}
