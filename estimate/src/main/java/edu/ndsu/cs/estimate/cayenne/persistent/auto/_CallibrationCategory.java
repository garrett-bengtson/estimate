package edu.ndsu.cs.estimate.cayenne.persistent.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.property.ListProperty;
import org.apache.cayenne.exp.property.NumericIdProperty;
import org.apache.cayenne.exp.property.PropertyFactory;
import org.apache.cayenne.exp.property.StringProperty;

import edu.ndsu.cs.estimate.cayenne.persistent.CallibrationExercise;
import edu.ndsu.cs.estimate.cayenne.persistent.CallibrationSuggestion;

/**
 * Class _CallibrationCategory was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _CallibrationCategory extends BaseDataObject {

    private static final long serialVersionUID = 1L;

    public static final NumericIdProperty<Integer> CATEGORY_ID_PK_PROPERTY = PropertyFactory.createNumericId("CategoryID", "CallibrationCategory", Integer.class);
    public static final String CATEGORY_ID_PK_COLUMN = "CategoryID";

    public static final StringProperty<String> DESCRIPTION = PropertyFactory.createString("description", String.class);
    public static final StringProperty<String> NAME = PropertyFactory.createString("name", String.class);
    public static final ListProperty<CallibrationExercise> EXERCISES = PropertyFactory.createList("exercises", CallibrationExercise.class);
    public static final ListProperty<CallibrationSuggestion> SUGGESTIONS = PropertyFactory.createList("suggestions", CallibrationSuggestion.class);

    protected String description;
    protected String name;

    protected Object exercises;
    protected Object suggestions;

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

    public void addToExercises(CallibrationExercise obj) {
        addToManyTarget("exercises", obj, true);
    }

    public void removeFromExercises(CallibrationExercise obj) {
        removeToManyTarget("exercises", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<CallibrationExercise> getExercises() {
        return (List<CallibrationExercise>)readProperty("exercises");
    }

    public void addToSuggestions(CallibrationSuggestion obj) {
        addToManyTarget("suggestions", obj, true);
    }

    public void removeFromSuggestions(CallibrationSuggestion obj) {
        removeToManyTarget("suggestions", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<CallibrationSuggestion> getSuggestions() {
        return (List<CallibrationSuggestion>)readProperty("suggestions");
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
            case "exercises":
                return this.exercises;
            case "suggestions":
                return this.suggestions;
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
            case "exercises":
                this.exercises = val;
                break;
            case "suggestions":
                this.suggestions = val;
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
        out.writeObject(this.exercises);
        out.writeObject(this.suggestions);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.description = (String)in.readObject();
        this.name = (String)in.readObject();
        this.exercises = in.readObject();
        this.suggestions = in.readObject();
    }

}
