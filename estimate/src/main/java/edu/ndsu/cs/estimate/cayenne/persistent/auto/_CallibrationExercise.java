package edu.ndsu.cs.estimate.cayenne.persistent.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.property.BaseProperty;
import org.apache.cayenne.exp.property.ListProperty;
import org.apache.cayenne.exp.property.NumericIdProperty;
import org.apache.cayenne.exp.property.PropertyFactory;
import org.apache.cayenne.exp.property.StringProperty;

import edu.ndsu.cs.estimate.cayenne.persistent.CallibrationCategory;
import edu.ndsu.cs.estimate.cayenne.persistent.CallibrationEstimate;

/**
 * Class _CallibrationExercise was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _CallibrationExercise extends BaseDataObject {

    private static final long serialVersionUID = 1L;

    public static final NumericIdProperty<Integer> EXERCISE_ID_PK_PROPERTY = PropertyFactory.createNumericId("ExerciseID", "CallibrationExercise", Integer.class);
    public static final String EXERCISE_ID_PK_COLUMN = "ExerciseID";

    public static final StringProperty<String> DESCRIPTION = PropertyFactory.createString("description", String.class);
    public static final StringProperty<String> NAME = PropertyFactory.createString("name", String.class);
    public static final BaseProperty<Boolean> OUTCOME = PropertyFactory.createBase("outcome", Boolean.class);
    public static final BaseProperty<Boolean> OUTCOME_REPORTED = PropertyFactory.createBase("outcomeReported", Boolean.class);
    public static final ListProperty<CallibrationCategory> CATEGORIES = PropertyFactory.createList("categories", CallibrationCategory.class);
    public static final ListProperty<CallibrationEstimate> ESTIMATES = PropertyFactory.createList("estimates", CallibrationEstimate.class);

    protected String description;
    protected String name;
    protected Boolean outcome;
    protected Boolean outcomeReported;

    protected Object categories;
    protected Object estimates;

    public void setDescription(String description) {
        beforePropertyWrite("description", this.description, description);
        this.description = description;
    }

    public String getDescription() {
        beforePropertyRead("description");
        return this.description;
    }

    public void setName(String name) {
        beforePropertyWrite("name", this.name, name);
        this.name = name;
    }

    public String getName() {
        beforePropertyRead("name");
        return this.name;
    }

    public void setOutcome(boolean outcome) {
        beforePropertyWrite("outcome", this.outcome, outcome);
        this.outcome = outcome;
    }

	public boolean isOutcome() {
        beforePropertyRead("outcome");
        if(this.outcome == null) {
            return false;
        }
        return this.outcome;
    }

    public void setOutcomeReported(boolean outcomeReported) {
        beforePropertyWrite("outcomeReported", this.outcomeReported, outcomeReported);
        this.outcomeReported = outcomeReported;
    }

	public boolean isOutcomeReported() {
        beforePropertyRead("outcomeReported");
        if(this.outcomeReported == null) {
            return false;
        }
        return this.outcomeReported;
    }

    public void addToCategories(CallibrationCategory obj) {
        addToManyTarget("categories", obj, true);
    }

    public void removeFromCategories(CallibrationCategory obj) {
        removeToManyTarget("categories", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<CallibrationCategory> getCategories() {
        return (List<CallibrationCategory>)readProperty("categories");
    }

    public void addToEstimates(CallibrationEstimate obj) {
        addToManyTarget("estimates", obj, true);
    }

    public void removeFromEstimates(CallibrationEstimate obj) {
        removeToManyTarget("estimates", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<CallibrationEstimate> getEstimates() {
        return (List<CallibrationEstimate>)readProperty("estimates");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "description":
                return this.description;
            case "name":
                return this.name;
            case "outcome":
                return this.outcome;
            case "outcomeReported":
                return this.outcomeReported;
            case "categories":
                return this.categories;
            case "estimates":
                return this.estimates;
            default:
                return super.readPropertyDirectly(propName);
        }
    }

    @Override
    public void writePropertyDirectly(String propName, Object val) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch (propName) {
            case "description":
                this.description = (String)val;
                break;
            case "name":
                this.name = (String)val;
                break;
            case "outcome":
                this.outcome = (Boolean)val;
                break;
            case "outcomeReported":
                this.outcomeReported = (Boolean)val;
                break;
            case "categories":
                this.categories = val;
                break;
            case "estimates":
                this.estimates = val;
                break;
            default:
                super.writePropertyDirectly(propName, val);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeSerialized(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        readSerialized(in);
    }

    @Override
    protected void writeState(ObjectOutputStream out) throws IOException {
        super.writeState(out);
        out.writeObject(this.description);
        out.writeObject(this.name);
        out.writeObject(this.outcome);
        out.writeObject(this.outcomeReported);
        out.writeObject(this.categories);
        out.writeObject(this.estimates);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.description = (String)in.readObject();
        this.name = (String)in.readObject();
        this.outcome = (Boolean)in.readObject();
        this.outcomeReported = (Boolean)in.readObject();
        this.categories = in.readObject();
        this.estimates = in.readObject();
    }

}
