package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataWriter;
import edu.cmu.tetrad.data.Knowledge;
import io.github.cmuphil.tetradfx.for751lib.ChangedStuffINeed;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Returns a Node that allows the user to filter variable names into tiers based on regexes. These tiers can then be
 * used for search, to specify temporal tiers where viariable in later tiers cannot cause variables in earlier tiers,
 *
 * @author josephramsey
 */
public class KnowledgeRegexFilter {
    private final VBox tierPanelContainer = new VBox(10);
    private final List<TextArea> displayAreas = new ArrayList<>();
    private final TextArea unmatchedVarsArea = new TextArea();
//    private final Map<Integer, String> rememberedRegexes = new HashMap<>();
    private final Knowledge knowledge;
    private final File path;
    private final SavedRegexesInfo savedRegexesInfo;

    /**
     * Creates a new KnowledgeEditor for the given Knowledge object.
     * @param knowledge The Knowledge object to edit.
     */
    public KnowledgeRegexFilter(Knowledge knowledge, File path) {
        System.out.println(path);


        System.out.println(path);

        this.knowledge = knowledge;
        this.path = path;
        File file = new File(path.toString() + ".regexes" + ".json");

        if (file.exists()) {
            savedRegexesInfo = (SavedRegexesInfo) ChangedStuffINeed.javaFromJson(file,
                    KnowledgeRegexFilter.SavedRegexesInfo.class);
        } else {
            savedRegexesInfo = new SavedRegexesInfo(knowledge.getVariables(), 2, new HashMap<>());
        }
    }

    public static class SavedRegexesInfo {
        private List<String> variables;
        private int tierCount;
        private Map<Integer, String> rememberedRegexes;

        public SavedRegexesInfo(List<String> variables, int tierCount, Map<Integer, String> rememberedRegexes) {
            this.variables = new ArrayList<>(variables);
            this.tierCount = tierCount;
            this.rememberedRegexes = new HashMap<>(rememberedRegexes);
        }

        public List<String> getVariables() {
            return new ArrayList<>(variables);
        }

        public int getTierCount() {
            return tierCount;
        }

        public void setTierCount(int tierCount) {
            this.tierCount = tierCount;
        }

        public Map<Integer, String> getRememberedRegexes() {
            return rememberedRegexes;
        }

        public void setRememberedRegexes(Map<Integer, String> rememberedRegexes) {
            this.rememberedRegexes = rememberedRegexes;
        }
    }

    /**
     * Returns a Node that allows the user to filter variable names into tiers based on regexes. These tiers can then be
     * used for search, to specify temporal tiers where viariable in later tiers cannot cause variables in earlier tiers,
     * This node should be places in a ScrollPane.
     * @return A Node that allows the user to filter variable names into tiers based on regexes.
     */
    public Node getEditor() {
        VBox root = new VBox(10);

        Label titleLabel = new Label("This component filters your variables names into tiers based on the regexes you provide.");

        HBox tierCountPanel = new HBox(10);

        Label tierCountLabel = new Label("Number of tiers:");
        TextField tierCountField = new TextField(savedRegexesInfo.tierCount + "");
        tierCountField.setPrefColumnCount(3);
        tierCountField.setPromptText("Enter number of tiers");
        tierCountPanel.getChildren().addAll(tierCountLabel, tierCountField);

        root.getChildren().addAll(titleLabel, tierCountPanel);

        TextArea inputArea = new TextArea();
        inputArea.setPromptText("Enter variable names here...");

        List<String> variables = savedRegexesInfo.getVariables();
        Iterator<String> iterator = variables.iterator();
        while (iterator.hasNext()) {
            String varName = iterator.next();

            if (inputArea.getText().isBlank()) {
                inputArea.appendText(varName);
            } else {
                inputArea.appendText(", " + varName);
            }
            iterator.remove();
        }

        inputArea.setMaxHeight(100);
        root.getChildren().add(inputArea);

        createTierPanels(savedRegexesInfo.getTierCount(), inputArea, savedRegexesInfo.getRememberedRegexes());

        root.getChildren().add(tierPanelContainer);

        // Unmatched variables TextArea
        unmatchedVarsArea.setPromptText("These are the variables that did not match any regex...");
        unmatchedVarsArea.setEditable(false);
        unmatchedVarsArea.setMaxHeight(100);
        root.getChildren().add(unmatchedVarsArea);

        tierCountField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int count = Integer.parseInt(newValue);
                createTierPanels(count, inputArea, savedRegexesInfo.getRememberedRegexes());
                updateDisplays(inputArea);
            } catch (NumberFormatException e) {
                tierPanelContainer.getChildren().clear();
            }
        });

        inputArea.textProperty().addListener((observable, oldValue, newValue) -> updateDisplays(inputArea));

        return root;
    }

    /**
     * Creates the tier panels based on the given count. The panels are added to the tierPanelContainer.
     * @param count The number of tier panels to create.
     * @param inputArea The TextArea containing the variable names.
     * @param rememberedRegexes A Map of tier index to remembered regexes.
     */
    private void createTierPanels(int count, TextArea inputArea, Map<Integer, String> rememberedRegexes) {
        tierPanelContainer.getChildren().clear();
        displayAreas.clear();

        for (int i = 0; i < count; i++) {
            int _i = i;

            HBox hBox = new HBox(10);

            TextArea regexArea = new TextArea();
            regexArea.setWrapText(true);
            regexArea.setMaxHeight(50);
            regexArea.setMaxWidth(200);
            regexArea.setPromptText("Enter regexes here...");
            regexArea.setText(rememberedRegexes.getOrDefault(_i, ""));

            TextArea displayArea = new TextArea();
            displayArea.setMaxHeight(100);
            displayArea.setWrapText(true);
            displayArea.setEditable(false);
            displayArea.setPromptText("Filtered matches appear here...");
            displayAreas.add(displayArea);

            regexArea.setOnKeyTyped(keyEvent -> {
                updateDisplays(inputArea);
                rememberedRegexes.put(_i, regexArea.getText());
            });

            hBox.getChildren().addAll(regexArea, displayArea);
            tierPanelContainer.getChildren().add(hBox);

            updateDisplays(inputArea);
        }
    }

    /**
     * Updates the display areas based on the given inputArea.
     * @param inputArea The TextArea containing the variable names.
     */
    private void updateDisplays(TextArea inputArea) {
        List<String> variableNames = new ArrayList<>(List.of(inputArea.getText().split("[,;\\t\\s]+")));

        for (TextArea displayArea : displayAreas) {
            displayArea.clear();
        }

        unmatchedVarsArea.clear();

        for (TextArea displayArea : displayAreas) {
            TextArea regexField = (TextArea) ((HBox) displayArea.getParent()).getChildren().get(0);

            List<Pattern> patterns = new ArrayList<>();

            try {
                String text = regexField.getText();
                String[] regexes = text.split("[,;\\t\\s]+");

                for (String regex : regexes) {
                    patterns.add(Pattern.compile(regex));
                }
            } catch (Exception e) {
                displayArea.setText("Invalid regex");
                continue;
            }

            Iterator<String> iterator = variableNames.iterator();
            while (iterator.hasNext()) {
                String varName = iterator.next();

                for (Pattern pattern : patterns) {
                    if (pattern.matcher(varName).find()) {
                        if (displayArea.getText().isBlank()) {
                            displayArea.appendText(varName);
                        } else {
                            displayArea.appendText(", " + varName);
                        }

                        iterator.remove();
                        break;
                    }
                }
            }
        }

        // For any remaining unmatched variables
        Iterator<String> iterator = variableNames.iterator();
        while (iterator.hasNext()) {
            String varName = iterator.next();

            if (unmatchedVarsArea.getText().isBlank()) {
                unmatchedVarsArea.appendText(varName);
            } else {
                unmatchedVarsArea.appendText(", " + varName);
            }

            iterator.remove();
        }

        knowledge.clear();

        for (int i = 0; i < displayAreas.size(); i++) {
            for (String varName : displayAreas.get(i).getText().split("[,;\\t\\s]+")) {
                knowledge.addToTier(i, varName);
            }
        }

        try {
            DataWriter.saveKnowledge(knowledge, new FileWriter(path));
            savedRegexesInfo.setTierCount(displayAreas.size());
            savedRegexesInfo.setRememberedRegexes(savedRegexesInfo.getRememberedRegexes());
            File file = new File(path.getAbsolutePath() + ".regexes" + ".json");
            ChangedStuffINeed.jsonFromJava(savedRegexesInfo, file);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error saving knowledge file.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
