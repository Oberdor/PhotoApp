package org.ks.photoapp.domain.photos;

import jakarta.persistence.*;
import lombok.Data;
import org.ks.photoapp.domain.photoSession.PhotoSession;

@Data
@Entity
public class Photos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Boolean sentToClientForChoose;
    private Boolean chosenByClient;
    private Boolean additionalChosenByClient;
    @OneToOne(mappedBy = "photos")
    private PhotoSession photoSession;
}
