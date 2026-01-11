package org.ks.photoapp.domain.photosession;

import org.ks.photoapp.domain.photosession.dto.PhotoSessionDto;

public class PhotoSessionDtoMapper {
    public static PhotoSessionDto map(PhotoSession photoSession) {
        return org.ks.photoapp.domain.photosession.mapper.PhotoSessionDtoMapper.map(photoSession);
    }
}
