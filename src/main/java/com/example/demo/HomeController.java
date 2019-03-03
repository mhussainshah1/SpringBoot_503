package com.example.demo;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    ActorRepository actorRepository;

    @Autowired
    CloudinaryConfig cloudc;

    @RequestMapping("/")
    public String listActor(Model model) {
        model.addAttribute("actors", actorRepository.findAll());//select * from Actor
        return "list";
    }

    @GetMapping("/add")
    public String newActor(Model model) {
        model.addAttribute("actor", new Actor());
        System.out.println("Get Mapping called");
        return "form";
    }

    /**
     * I have found weird thing. We gotta put @Valid and BinddingResult parameter together
     * otherwise page move to another page
     *
     */
    @PostMapping("/process")
    public String processActor(@Valid Actor actor,
                               BindingResult result,
                               @RequestParam("file") MultipartFile file) {
        System.out.println("Post Mapping called");
        if (result.hasErrors() || file.isEmpty()) {
            return "form";//"redirect:/add";
        }

        try {
            Map uploadResult = cloudc.upload(
                    file.getBytes(), ObjectUtils.asMap("resourcetype", "auto"));
            String uploadURL = uploadResult.get("url").toString();
            String uploadedName = uploadResult.get("public_id").toString();
            String transformedImage = cloudc.createUrl(uploadedName);

            System.out.println("Uploaded Url:" + uploadURL);
            System.out.println("Uploaded File Name:" + uploadedName);
            System.out.println("Transformed Url:" + transformedImage);

            actor.setHeadshot(transformedImage);
            actorRepository.save(actor);
            // Insert into Actor (name,realname,headshot)
            // values (<<name>>,<<realname>>,<<headshot>>) where id = actor.id
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/form";
        }
        return "redirect:/";
    }
}
