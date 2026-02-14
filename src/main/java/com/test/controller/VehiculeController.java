package com.test.controller;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fw.ModelView;
import com.fw.annotations.AnnotationController;
import com.fw.annotations.ManageUrl;
import com.fw.annotations.MyGET;
import com.fw.annotations.MyPOST;
import com.test.model.Vehicule;
import com.test.repository.VehiculeRepository;
import jakarta.servlet.http.HttpServletRequest;


@AnnotationController("/vehicule")
public class VehiculeController {
    
    private final VehiculeRepository vehiculeRepository = new VehiculeRepository();

    @ManageUrl("/list")
    @MyGET
    public ModelView list() {
        try {

            List<Vehicule> vehicules = vehiculeRepository.findAll();
            ModelView mv = new ModelView("layout.jsp");
            mv.addItem("content", "vehicule/vehicule_list.jsp");
            mv.addItem("vehicules", vehicules);
            return mv;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ManageUrl("/{id}")
    @MyGET
    public ModelView read(int id) {
        try {
            ModelView mv = new ModelView("layout.jsp");
            mv.addItem("content", "vehicule/vehicule_detail.jsp");
            Vehicule vehicule = vehiculeRepository.getById(id);
            mv.addItem("vehicule", vehicule);
            return mv;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ManageUrl("/save") 
    @MyPOST
    public ModelView save(String reference, Integer place, String typeCarburant, HttpServletRequest request) {
        try {
            Vehicule vehicule = new Vehicule();

            String idParam = request.getParameter("id");
            if (idParam != null && !idParam.isEmpty()) {
                try {
                    Integer parsedId = Integer.valueOf(idParam);
                    vehicule.setId(parsedId);
                } catch (NumberFormatException nfe) {
                    // ignore invalid id, treat as new
                }
            }

            vehicule.setReference(reference);
            vehicule.setPlace(place);
            vehicule.setTypeCarburant(typeCarburant);

            ModelView mv = new ModelView("layout.jsp");
            mv.addItem("content", "vehicule/vehicule_detail.jsp");
            Vehicule savedVehicule = vehiculeRepository.save(vehicule);
            mv.addItem("vehicule", savedVehicule);
            return mv;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @ManageUrl("/form")
    @MyGET
    public ModelView createForm() {

        ModelView mv = new ModelView("layout.jsp");
        mv.addItem("content", "vehicule/vehicule_form.jsp");
        return mv;
       
    }

    @ManageUrl("/form/{id}")
    @MyGET
    public ModelView editForm(Integer id) {

        ModelView mv = new ModelView("layout.jsp");
        mv.addItem("content", "vehicule/vehicule_form.jsp");
        try {
            Vehicule vehicule = vehiculeRepository.getById(id);
            mv.addItem("vehicule", vehicule);
            return mv;
        } catch (Exception e) {
            throw new RuntimeException("Error while going to vehicule form", e);
        }
       
    }

    @ManageUrl("/delete/{id}")
    @MyGET
    public ModelView delete(Integer id) {
        try {
            vehiculeRepository.deleteById(id);
            return list();
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting vehicule with id = " + id, e);
        }
    }
}
