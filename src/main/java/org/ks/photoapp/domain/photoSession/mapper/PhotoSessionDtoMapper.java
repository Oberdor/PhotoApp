package org.ks.photoapp.domain.photosession.mapper;

import org.ks.photoapp.domain.photosession.PhotoSession;
import org.ks.photoapp.domain.photosession.dto.PhotoSessionDto;

public class PhotoSessionDtoMapper {
    public static PhotoSessionDto map(PhotoSession photoSession) {
        return new PhotoSessionDto(
                photoSession.getClient(),
                photoSession.getId(),
                photoSession.getSessionDate(),
                photoSession.getSessionType(),
                photoSession.getPayment().getIsDepositPaid(),
                photoSession.getPayment().getIsBasePaid(),
                photoSession.getPhotos().getSentToClientForChoose(),
                photoSession.getPhotos().getChosenByClient(),
                photoSession.getPhotos().getAdditionalChosenByClient(),
                photoSession.getPayment().getIsAdditionalPaid(),
                photoSession.getIsContractFinished()
        );
    }
}
