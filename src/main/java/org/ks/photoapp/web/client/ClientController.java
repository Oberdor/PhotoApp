package org.ks.photoapp.web.client;


import org.ks.photoapp.domain.client.dto.ClientDto;
import org.ks.photoapp.domain.photosession.PhotoSessionService;
import org.ks.photoapp.domain.photosession.dto.PhotoSessionDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class ClientController{
    org.ks.photoapp.domain.client.ClientService clientService;
    PhotoSessionService photoSessionService;
    public static final String NOTIFICATION_ATTRIBUTE = "notification";

    public ClientController(org.ks.photoapp.domain.client.ClientService clientService, PhotoSessionService photoSessionService) {
        this.clientService = clientService;
        this.photoSessionService = photoSessionService;
    }

    @GetMapping("/client/current")
    public String getAllCurrentClients(Model model) {
        List<ClientDto> clients = clientService.getAllCurrentClients();
        model.addAttribute("heading", "Aktualni klienci");
        model.addAttribute("clients", clients);
        return "current-client";
    }

    @GetMapping("/client/all")
    public String getAllClients(Model model) {
        List<ClientDto> clients = clientService.getAllClients();
        model.addAttribute("heading", "Wszyscy klienci");
        model.addAttribute("clients", clients);
        return "all-clients";
    }

    @GetMapping("/client/{id}")
    public String getClientById(@PathVariable long id, Model model){
        Optional<ClientDto> optionalClient = clientService.findClientById(id);
        Optional<PhotoSessionDto> optionalPhotoSession = photoSessionService.getPhotoSessionByClientId(id);
        optionalClient.ifPresent(client -> model.addAttribute("client", client));
        optionalPhotoSession.ifPresent(photoSession -> model.addAttribute("photoSession", photoSession));
        return "client";
    }

    @GetMapping("/client/new")
    public String newClientForm(Model model){
        ClientDto client = new ClientDto();
        model.addAttribute("client", client);
        return "client-form";
    }

    @PostMapping("/client/new")
    public String newClient(ClientDto client, RedirectAttributes redirectAttributes){
        clientService.createNewClient(client);
        addNotification(redirectAttributes, "Dodano nowego klienta");
        return "redirect:/client/all";
    }


    @GetMapping("/client/delete")
    public String deleteClient(@RequestParam long id, RedirectAttributes redirectAttributes){
        clientService.deleteClient(id);
        addNotification(redirectAttributes, "Usunięto klienta");
        return "redirect:/all-clients";
    }

    @PostMapping("/client/update")
    public String updateClient(@RequestParam long id, ClientDto client, RedirectAttributes redirectAttributes){
        clientService.updateClientDetails(client, id);
        addNotification(redirectAttributes, "Dane klienta zaktualinowane");
        return "redirect:/client/{id}";
    }

    private void addNotification(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(NOTIFICATION_ATTRIBUTE, message);
    }
}
