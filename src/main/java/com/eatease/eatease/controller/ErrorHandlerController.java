package com.eatease.eatease.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorHandlerController implements ErrorController {

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        // Get error status code
        Object status = request.getAttribute("javax.servlet.error.status_code");

        ModelAndView modelAndView = new ModelAndView("error");

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            modelAndView.addObject("code", statusCode);
            modelAndView.addObject("message", HttpStatus.valueOf(statusCode).getReasonPhrase());

            // You can add different logic based on status code
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                modelAndView.addObject("customMessage", "A página que você está procurando não existe.");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                modelAndView.addObject("customMessage", "Você não tem permissão para acessar esta página.");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                modelAndView.addObject("customMessage", "Ocorreu um erro interno no servidor.");
            } else {
                modelAndView.addObject("customMessage", "Ocorreu um erro ao processar a sua solicitação.");
            }
        } else {
            modelAndView.addObject("code", "Erro");
            modelAndView.addObject("message", "Erro desconhecido");
            modelAndView.addObject("customMessage", "Ocorreu um erro inesperado.");
        }

        return modelAndView;
    }
}