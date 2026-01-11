package org.ks.photoapp.domain.client;



import org.ks.photoapp.domain.client.dto.ClientDto;
import org.ks.photoapp.domain.photoSession.PhotoSessionService;
import org.ks.photoapp.domain.photoSession.dto.PhotoSessionDto;
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
    private final ClientService clientService;
    private final PhotoSessionService photoSessionService;
    public static final String NOTIFICATION_ATTRIBUTE = "notification";

    public ClientController(ClientService clientService, PhotoSessionService photoSessionService) {
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

   // @GetMapping("/client/{lastName}")
  //  public String getClientByLastName(@PathVariable String lastName, Model model){
  //     ClientDto client = clientService.findClientByLastName(lastName)
   //             .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
   //     model.addAttribute("client", client);
  //      return "current-client";
 //   }

    @GetMapping("/client/new")
    public String newClientForm(Model model){
        ClientDto client = new ClientDto();
        model.addAttribute("client", client);
        return "client-form";
    }

    @PostMapping("/client/new")
    public String newClient(ClientDto client, RedirectAttributes redirectAttributes){
        clientService.createNewClient(client);
        redirectAttributes.addFlashAttribute(NOTIFICATION_ATTRIBUTE,
                "Dodano nowego klienta");
        return "redirect:/client/all";
    }



    @GetMapping("/client/delete")
    public String deleteClient(@RequestParam long id, RedirectAttributes redirectAttributes){
        clientService.deleteClient(id);
        redirectAttributes.addFlashAttribute(NOTIFICATION_ATTRIBUTE,
                "Usunięto klienta");
        return "redirect:/client/all";
    }

    @PostMapping("/client/update")
    public String updateClient(@RequestParam long id, ClientDto client, RedirectAttributes redirectAttributes){
        clientService.updateClientDetails(client, id);
        redirectAttributes.addFlashAttribute(NOTIFICATION_ATTRIBUTE,
                "Dane klienta zaktualinowane");
        return "redirect:/client/" + id;
    }
}
