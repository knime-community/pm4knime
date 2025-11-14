### Running the Extension in Development Mode (Eclipse)

#### Prerequisites
- Java 11 or higher
- Eclipse for RCP and RAP developers

#### Setup Instructions

[1] **Install Java**: Ensure Java 11 or a newer version is installed on your system.  

[2] **Install Eclipse**: Download and install Eclipse for RCP and RAP developers from the Eclipse official website.

[3] **Install Apache Ivy**:
   - Navigate to `Help` -> `Install New Software` -> `Add`.
   - Enter `Name`: `Apache Ivy` and `Location`: `https://archive.apache.org/dist/ant/ivyde/updatesite/`.
   - Select all, then follow the on-screen instructions to complete the installation.

[4] **Configure Java in Eclipse**:
   - Go to `Window` -> `Preferences` -> `Java` -> `Installed JREs` -> `Add`.
   - Choose `Standard VM`, then add the path to your JRE directory.
   - Navigate to `Java` -> `Compiler` and set the compiler compliance level to match your installed Java version.

[5] **Clone the Project Using Git**:
   - Use Eclipse's Git support to clone the project repository.  
   - The project will be automatically imported into Eclipse.

[6] **Set Active Target Platform**:
   - Double-click the `KNIME-AP.target` file in the `org.knime.sdk.setup` directory.
   - Click `Set as Active Target Platform` at the top right corner.

[7] **Build the Project**:
   - Right-click the `build.xml` file in the `org.pm4knime.java` directory.
   - Select `Run As` -> `Ant Build`.

[8] **Run the Project**:
   - In `org.knime.sdk.setup`, right-click `KNIME Analytics Platform.launch`.
   - Select `Run As`.

### Activating Python Nodes in Development Mode (Eclipse)

[1] **Install Pixi and Set Up Python Environment**:
   - Download Pixi using the following documentation: [https://pixi.sh/v0.47.0/#installation](https://pixi.sh/v0.47.0/#installation)
   - Open a terminal in the root directory of the project and run the following commands:
     ```
     set PATH=%PATH%;<path_to_pixi>\bin
     pixi update
     pixi install
     ```

[2] **Create `config.yml`**:
   - Create a `config.yml` inside the Python subfolder (`..\pm4knime-python\org.pm4knime.python`) with the following structure: <br>
     `org.pm4knime.python: # {group_id}.{name} from the knime.yml` 
       `src: <path_to_pm4knime-python>\org.pm4knime.python`
       `conda_env_path: <path_to_pm4knime-python>\.pixi\envs\default` 
       `debug_mode: true`

[3] **Run KNIME with Python Nodes**:
   - Go to `..\pm4knime-python\org.knime.sdk.setup`.
   - Right-click `KNIME Analytics Platform.launch` and select `Run As` -> `1 KNIME Analytics Platform`.
   - Python nodes should now be available in KNIME.

### Workflow Tests

For information on how to add and execute test workflows, please refer to the [KNIME Testflow documentation](https://github.com/3D-e-Chem/knime-testflow#3-add-test-workflow).
