/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import play.db.ebean.Transactional;

import play.mvc.*;
import play.data.*;

import static play.data.Form.*;

import models.*;
import views.formdata.InputClient;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import java.util.List;
import java.util.ArrayList;
import play.Logger;

/**
 * Manage a database of users
 */
public class ClientController extends Controller {

    private FormFactory formFactory;

    @Inject
    public ClientController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    /**
     * Display the list of users.
     */
    @Security.Authenticated(Secured.class)
    public Result list() {
        return ok(views.html.client_list.render(Client.list(), Secured.getClient(ctx())));
    }

    public Result LIST() {
        return Results.redirect(routes.ClientController.list());
    }

    /**
     * Display the 'edit form' of an existing Technician.
     *
     * @param id Id of the user to edit
     */
    @Security.Authenticated(Secured.class)
    public Result edit(Long id) {
        Form<InputClient> clientForm = formFactory.form(InputClient.class).fill(new InputClient(Client.find.byId(id)));
        if (Secured.isAdmin(ctx())) {
            return ok(views.html.client_editForm.render(id, clientForm));
        } else {
            return HomeController.PROBLEM("Non administrators cannot change clients.");
        }
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the client to edit
     */
    @Transactional
    @Security.Authenticated(Secured.class)
    public Result update(Long id) throws PersistenceException {
        Form<InputClient> clientForm = formFactory.form(InputClient.class).bindFromRequest();
        if (clientForm.hasErrors()) {
            return badRequest(views.html.client_editForm.render(id, clientForm));
        }
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            clientForm.withError("adminstrator", "Non administrators cannot change clients.");
            return badRequest(views.html.client_editForm.render(id, clientForm));
        }
        InputClient updateClient = clientForm.get();
        if (Client.hasClientWithName(updateClient.name, id.intValue())) {
            return badRequest("Already a client with name: " + updateClient.name);
        }
        Client client = Client.find.byId(id);
        client.name = updateClient.name;
        client.password = updateClient.password;
        client.is_admin = updateClient.isAdmin;
        client.update();

        ClientProjectAssociation.process(id, getCheckedProjects(clientForm));
        ClientCompanyNameAssociation.process(id, clientForm);
        ClientAssociation.process(client, clientForm);

        return LIST();
    }

    /**
     * Display the 'new client form'.
     */
    @Security.Authenticated(Secured.class)
    public Result create() {
        Form<InputClient> clientForm = formFactory.form(InputClient.class).fill(new InputClient());
        return ok(views.html.client_createForm.render(clientForm));
    }

    /**
     * Handle the 'new client form' submission
     */
    @Security.Authenticated(Secured.class)
    @Transactional
    public Result save() {
        Form<InputClient> clientForm = formFactory.form(InputClient.class).bindFromRequest();
        if (clientForm.hasErrors()) {
            return badRequest(views.html.client_createForm.render(clientForm));
        }
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            clientForm.withError("adminstrator", "Non administrators cannot create clients.");
            return badRequest(views.html.client_createForm.render(clientForm));
        }
        InputClient inputClient = clientForm.get();
        if (inputClient.name == null || inputClient.name.isEmpty()) {
            return badRequest("No client name entered.");
        }
        if (inputClient.password == null || inputClient.password.isEmpty()) {
            return badRequest("No client password entered.");
        }
        if (Client.getUser(inputClient.name) != null) {
            return badRequest("Already have a client named: " + inputClient.name);
        }
        Client newClient = new Client();
        newClient.name = inputClient.name;
        newClient.password = inputClient.password;
        newClient.is_admin = inputClient.isAdmin;
        newClient.save();

        long id = newClient.id;

        ClientProjectAssociation.process(id, getCheckedProjects(clientForm));
        ClientCompanyNameAssociation.process(id, clientForm);
        ClientAssociation.process(newClient, clientForm);

        return LIST();
    }

    List<Project> getCheckedProjects(Form<InputClient> clientForm) {
        List<Project> projects = new ArrayList<Project>();
        for (Project project : Project.list()) {
            try {
                if (clientForm.field(project.getFullProjectName()).getValue().get().equals("true")) {
                    projects.add(project);
                }
            } catch (Exception ex) {
            }
        }
        return projects;
    }

    /**
     * Handle user deletion
     */
    @Security.Authenticated(Secured.class)
    @Transactional
    public Result delete(Long id) {
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            Form<InputClient> clientForm = formFactory.form(InputClient.class).bindFromRequest();
            clientForm.withError("adminstrator", "Non administrators cannot delete clients.");
            return badRequest(views.html.client_editForm.render(id, clientForm));
        }
        Client client = Client.find.byId(id);
        if (client.is_admin) {
            return badRequest("You cannot delete an adminstrator");
        }
        boolean disable = false;
        List<Equipment> equipments = Equipment.getCreatedByClient(id.intValue());
        for (Equipment equipment : equipments) {
            if (Entry.hasEntryForEquipment(equipment.id)) {
                disable = true;
            } else {
                equipment.delete();
            }
        }
        List<Note> notes = Note.getCreatedByClient(id.intValue());
        for (Note note : notes) {
            if (Entry.hasEntryForNote(note.id)) {
                disable = true;
            } else {
                note.delete();
            }
        }
        List<Company> companies = Company.getCreatedByClient(id.intValue());
        for (Company company : companies) {
            if (Entry.hasEntryForCompany(company.id)) {
                disable = true;
            } else {
                company.delete();
            }
        }
        List<WorkOrder> workorders = WorkOrder.findByClientId(id);
        for (WorkOrder order : workorders) {
            order.delete();
        }
        ClientProjectAssociation.deleteEntries(id);
        if (disable) {
            client.disabled = true;
            client.update();
            flash("success", "Client has been disabled");
        } else {
            ClientAssociation.deleteEntries(id);
            ClientCompanyNameAssociation.deleteEntries(id);
            ClientEquipmentAssociation.deleteEntries(id);
            ClientNoteAssociation.deleteEntries(id);
            Client.find.ref(id).delete();
            flash("success", "Client has been deleted");
        }
        return LIST();
    }

}
