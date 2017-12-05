package views.formdata;

import play.data.validation.ValidationError;
import java.util.ArrayList;
import java.util.List;
import models.Client;

/**
 * Backing class for the login form.
 */
public class WorkOrderFormData {

    public String truck_number = "";
    public String license_plate = "";

    /**
     * Required for form instantiation.
     */
    public WorkOrderFormData() {
    }

    /**
     * Validates Form<WorkOrderFormData>.
     * Called automatically in the controller by bindFromRequest().
     * Checks to see that email and password are valid credentials.
     *
     * @return Null if valid, or a List[ValidationError] if problems found.
     */
    public List<ValidationError> validate() {
      List<ValidationError> errors = new ArrayList<>();
      if (!WorkOrderController.isValidTruckNumber(truck_number)) {
        errors.add(new ValidationError("truck_number", ""));
      }
      return (errors.size() > 0) ? errors : null;
    }

}
