package org.ks.photoapp.domain.photoSession.dto;

import lombok.Data;
import org.ks.photoapp.domain.client.Client;
import org.ks.photoapp.domain.sessionType.SessionType;


import java.time.LocalDateTime;


@Data
public class PhotoSessionDto {
    private Client client;
    private Long sessionPhotoId;
    private LocalDateTime sessionDate;
    private SessionType sessionType;
    private Boolean isDepositPaid;
    private Boolean isBasePaid;
    private Boolean isPhotosSentToClientForChoose;
    private Boolean isPhotosChosenByClient;
    private Boolean isAdditionalPhotosChosenByClient;
    private Boolean isAdditionalPaid;
    private Boolean isContractFinished;

    public PhotoSessionDto() {
    }

    public PhotoSessionDto(Client client, Long sessionPhotoId, LocalDateTime sessionDate, SessionType sessionType,
                           Boolean isDepositPaid, Boolean isBasePaid, Boolean isPhotosSentToClientForChoose,
                           Boolean isPhotosChosenByClient, Boolean isAdditionalPhotosChosenByClient,
                           Boolean isAdditionalPaid, Boolean isContractFinished) {
        this.client = client;
        this.sessionPhotoId = sessionPhotoId;
        this.sessionDate = sessionDate;
        this.sessionType = sessionType;
        this.isDepositPaid = isDepositPaid;
        this.isBasePaid = isBasePaid;
        this.isPhotosSentToClientForChoose = isPhotosSentToClientForChoose;
        this.isPhotosChosenByClient = isPhotosChosenByClient;
        this.isAdditionalPhotosChosenByClient = isAdditionalPhotosChosenByClient;
        this.isAdditionalPaid = isAdditionalPaid;
        this.isContractFinished = isContractFinished;
    }

}
