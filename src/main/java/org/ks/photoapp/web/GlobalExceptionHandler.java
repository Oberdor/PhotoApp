package org.ks.photoapp.web;

import org.ks.photoapp.domain.client.exception.ClientNotFoundException;
import org.ks.photoapp.domain.photosession.exception.PhotoSessionNotFoundException;
import org.ks.photoapp.domain.photosession.exception.SessionUpdateNotAllowedException;
import org.ks.photoapp.domain.payment.exception.PaymentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ClientNotFoundException.class, PhotoSessionNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(SessionUpdateNotAllowedException.class)
    public ModelAndView handleUpdateNotAllowed(SessionUpdateNotAllowedException ex) {
        ModelAndView mav = new ModelAndView("redirect:/all-photosessions");
        mav.addObject("notification", ex.getMessage());
        return mav;
    }

}
