// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.io.File;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;
import com.pathplanner.lib.path.PathPlannerPath;

import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;
import frc.robot.Constants.ArmConstants;
import frc.robot.Constants.BoxConstants;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.ArmConstants.ArmState;
//import frc.robot.commands.IdleArmCommand;
import frc.robot.subsystems.ArmSubsystem;
import frc.robot.subsystems.BoxSubsystem;
//import frc.robot.subsystems.LimelightSubsystem;
import frc.robot.subsystems.SwerveSubsystem;



// This class is instantiated when the robot is first started up
public class RobotContainer {
  // Make the one and only object of each subsystem
  private final SwerveSubsystem m_swerve = new SwerveSubsystem(new File (Filesystem.getDeployDirectory(), "swerve"));
  private final BoxSubsystem m_box = new BoxSubsystem();
  private final ArmSubsystem m_arm = new ArmSubsystem();
  //private final LimelightSubsystem m_limelight = new LimelightSubsystem(m_swerve);
  // Auton chooser
  private SendableChooser<Command> m_autonChooser = new SendableChooser<>();

  // Create the driver and operator controllers
  private final CommandXboxController m_driverController =
      new CommandXboxController(OperatorConstants.kDriverControllerPort);
  private final CommandXboxController m_operatorController =
      new CommandXboxController(OperatorConstants.kOperatorControllerPort);
  
  //private double climbingSlowMode = 1.0;
  
  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    // Setup PathPlanner and autons
    m_swerve.setupPathPlannerRobot();
    // PathPlanner named commands
    NamedCommands.registerCommand("ArmToFloor", m_arm.setArmPIDCommand(ArmConstants.ArmState.FLOOR, true).withTimeout(0.25));
    NamedCommands.registerCommand("ArmToSubwoofer", m_arm.setArmPIDCommand(ArmConstants.ArmState.SHOOT_SUB, true).withTimeout(0.25));
    NamedCommands.registerCommand("Intake", m_box.setIntakeMotorCommandThenStop(BoxConstants.kIntakeSpeed).withTimeout(1.75));
      NamedCommands.registerCommand("FRESH Intake", m_box.setIntakeMotorCommandThenStop(BoxConstants.kIntakeSpeed).until(m_box::noteSensorTriggered));
    NamedCommands.registerCommand("SpinUpShooter", m_box.setShooterMotorCommand(BoxConstants.kTopSpeakerRPM).withTimeout(1));
    NamedCommands.registerCommand("FeedNote", m_box.setIntakeMotorCommand(BoxConstants.kFeedSpeed).withTimeout(0.75));
    NamedCommands.registerCommand("ShootNoteSubwoofer", m_box.ShootNoteSubwoofer().withTimeout(2.25));
    NamedCommands.registerCommand("ShootNoteSubwooferNoRegurgitate", m_box.ShootNoteSubwooferNoRegurgitate().withTimeout(3.75));
    NamedCommands.registerCommand("ArmToIdle", m_arm.setArmPIDCommand(ArmConstants.ArmState.IDLE, true).withTimeout(1.5));
    NamedCommands.registerCommand("FireNote", m_box.setShooterFeederCommand(ArmSubsystem::getArmState, false).withTimeout(1));
    // Allows us to pick our auton in smartdash board
    m_autonChooser.addOption("TS-N1-C1", AutoBuilder.buildAuto("TS-N1-C1"));
    m_autonChooser.addOption("Drive Straight", AutoBuilder.buildAuto("Drive Straight"));
    SmartDashboard.putData("Auton Picker", m_autonChooser);
    //m_autonChooser = AutoBuilder.buildAutoChooser();
    //SmartDashboard.putData("Auton Picker", m_autonChooser);

    // Configure the trigger bindings
    configureBindings();

    // This causes a command scheduler loop overrun. Not sure why
    m_swerve.setDefaultCommand(m_swerve.driveCommandAngularVelocity(
      () -> -m_driverController.getLeftY(),
      () -> -m_driverController.getLeftX(),
      () -> -m_driverController.getRightX(),
      OperatorConstants.kFastModeSpeed,// * climbingSlowMode,
      true
    ));

    m_box.setDefaultCommand(m_box.stopCommand());
  }


  private void configureBindings() {
    /* Driver Controls */

    // Rotate towards the driver
  /*  m_driverController.a().whileTrue(m_swerve.driveCommandPoint(() -> -m_driverController.getLeftY(), () -> -m_driverController.getLeftX(),
      () -> 0,
      () -> -1
    ));

    // Rotate to the right
    m_driverController.b().whileTrue(m_swerve.driveCommandPoint(() -> -m_driverController.getLeftY(), () -> -m_driverController.getLeftX(),
      () -> 1,
      () -> 0
    ));

    // Rotate to the left
    m_driverController.x().whileTrue(m_swerve.driveCommandPoint(() -> -m_driverController.getLeftY(), () -> -m_driverController.getLeftX(),
      () -> -1,
      () -> 0
    ));
    
    // Rotate away from the driver
    m_driverController.y().whileTrue(m_swerve.driveCommandPoint(() -> -m_driverController.getLeftY(), () -> -m_driverController.getLeftX(),
      () -> 0,
      () -> 1
    ));
    */

    // Resets the gyro
    m_driverController.back().onTrue(
      m_swerve.runOnce(()->{
        m_swerve.resetGyro();
      })
    );
    
    // Medium speed
    m_driverController.rightTrigger().whileTrue(
      m_swerve.driveCommandAngularVelocity(
        () -> -m_driverController.getLeftY(),
        () -> -m_driverController.getLeftX(),
        () -> -m_driverController.getRightX(),
        OperatorConstants.kMidModeSpeed, 
        true
      )
    );
  
    // Slow speed
    m_driverController.leftTrigger().whileTrue(
      m_swerve.driveCommandAngularVelocity(
        () ->  -m_driverController.getLeftY(),
        () ->  -m_driverController.getLeftX(),
        () -> -m_driverController.getRightX(),
        OperatorConstants.kSlowModeSpeed,
        true
      )
    );


    //Robot Centric DRIVING
/*
    m_driverController.rightBumper().whileTrue(
      m_swerve.driveCommandAngularVelocity(
        () -> -m_driverController.getLeftY(),
        () -> -m_driverController.getLeftX(),
        () -> -m_driverController.getRightX(),
        OperatorConstants.kMidModeSpeed, 
        false
      )
    );
*/
    
    // Medium speed robot centric
    //m_driverController.rightTrigger().and(m_driverController.rightBumper()).whileTrue(
    m_driverController.rightBumper().whileTrue(
      m_swerve.driveCommandAngularVelocity(
        () -> -m_driverController.getLeftY(),
        () -> -m_driverController.getLeftX(),
        () -> -m_driverController.getRightX(),
        OperatorConstants.kMidModeSpeed, 
        false
      )
    );
      
    // Slow speed robot centric
    //m_driverController.leftTrigger().and(m_driverController.leftBumper()).whileTrue(
    m_driverController.leftBumper().whileTrue(
      m_swerve.driveCommandAngularVelocity(
        () ->  -m_driverController.getLeftY(),
        () ->  -m_driverController.getLeftX(),
        () -> -m_driverController.getRightX(),
        OperatorConstants.kSlowModeSpeed,
        false
      )
    );

    /* Operator Controls */

    // Sets the speed of the shooter motors and starts intake/feed motor
    // When the button is released, the arm goes to idle position and the m_box default command is ran
    m_operatorController.leftTrigger().whileTrue(
      m_box.setShooterFeederCommand(ArmSubsystem::getArmState, true)
    ).onFalse(m_arm.setArmPIDCommand(ArmConstants.ArmState.IDLE, false));

    // Intakes note into robot
    m_operatorController.leftBumper().whileTrue(m_box.setIntakeMotorCommand(BoxConstants.kIntakeSpeed));

    // Regurgitate everything
    m_operatorController.rightBumper().whileTrue(m_box.YeetCommand(BoxConstants.kRegurgitateSpeed, BoxConstants.kRegurgitateSpeed));

    // Smartshoot button, only shoots the note when Velocity is correct and the button is held down.
    m_operatorController.rightTrigger().whileTrue(
      Commands.sequence(
        Commands.waitUntil(m_box::isVelocityReached),
        m_box.setShooterFeederCommand(ArmSubsystem::getArmState, true)
      )
    ).onFalse(m_arm.setArmPIDCommand(ArmConstants.ArmState.IDLE, false));
  
    // Arm set point for climbing
    m_operatorController.button(9).whileTrue(
      m_arm.setArmPIDCommand(ArmConstants.ArmState.CLIMB_1, false)
    );

    m_operatorController.button(10).onTrue(m_arm.setArmPIDCommand(ArmConstants.ArmState.CLIMB_2, true));
    
      // Arm set point for shooting speaker from subwoofer
    m_operatorController.a().whileTrue(
      Commands.parallel(
        m_arm.setArmPIDCommand(ArmConstants.ArmState.SHOOT_SUB, true),
        m_box.setShooterFeederCommand(ArmSubsystem::getArmState, false)
      )
    ).onFalse(m_arm.setArmPIDCommand(ArmConstants.ArmState.IDLE, false));

    // Arm set point for playing amp
    m_operatorController.x().whileTrue(
      Commands.parallel(
        m_arm.setArmPIDCommand(ArmConstants.ArmState.AMP, true),
        m_box.setShooterFeederCommand(ArmSubsystem::getArmState, false)
      )
    ).onFalse(m_arm.setArmPIDCommand(ArmConstants.ArmState.IDLE, false));

    // Idle mode arm set point
    m_operatorController.b().whileTrue(m_arm.setArmPIDCommand(ArmConstants.ArmState.IDLE, false));

    // Arm set point for shooting podium
    m_operatorController.y().whileTrue(
      Commands.parallel(
        m_arm.setArmPIDCommand(ArmConstants.ArmState.SHOOT_PODIUM, true),
        m_box.setShooterFeederCommand(ArmSubsystem::getArmState, false)
      )
    ).onFalse(m_arm.setArmPIDCommand(ArmConstants.ArmState.IDLE, false));

    // Arm set point for shooting horizontal across the field
    m_operatorController.povLeft().whileTrue(
      Commands.parallel(
        m_arm.setArmPIDCommand(ArmConstants.ArmState.SHOOT_HORIZONTAL, true),
        m_box.setShooterFeederCommand(ArmSubsystem::getArmState, false)
      )
    ).onFalse(m_arm.setArmPIDCommand(ArmConstants.ArmState.IDLE, false));

    // Arm set point for shooting trap
    m_operatorController.povRight().whileTrue(
      Commands.parallel(
        m_arm.setArmPIDCommand(ArmConstants.ArmState.TRAP, true),
        m_box.setShooterFeederCommand(ArmSubsystem::getArmState, false)
      )
    ).onFalse(m_arm.setArmPIDCommand(ArmConstants.ArmState.IDLE, false));

    // Manual control toggle for arm
    m_operatorController.start().toggleOnTrue(
        m_arm.manualArmCommand(() -> m_operatorController.getRightY() * Constants.ArmConstants.kManualSpeed, 
        () -> m_operatorController.getLeftY() * Constants.ArmConstants.kManualSpeed)
    );

    // Smart floor intake with regurgitate?
    m_operatorController.povDown().whileTrue(
      Commands.sequence(
        Commands.parallel(
          m_arm.setArmPIDCommand(ArmConstants.ArmState.FLOOR, false),
          m_box.setIntakeMotorCommand(BoxConstants.kIntakeSpeed)
        ).until(m_box::noteSensorTriggered)
      )
    );

    // Intake from the source
    m_operatorController.povUp().whileTrue(
      Commands.sequence(
        Commands.parallel(
          m_arm.setArmPIDCommand(ArmConstants.ArmState.SOURCE, false),
          m_box.setIntakeMotorCommand(BoxConstants.kSourceIntakeSpeed)
        ).until(m_box::noteSensorTriggered).andThen(m_arm.setArmPIDCommand(ArmConstants.ArmState.IDLE, false))
      )
    );

    // Reset wrist encoder
    m_operatorController.back().onTrue(Commands.runOnce(() -> m_arm.resetWristEncoder()));



    /* KEY BINDS WE NEVER USE BUT COULD BE USEFUL? */

    /*
    // Alternate drive mode
    m_driverController.rightBumper().toggleOnTrue(m_swerve.driveCommandPoint(
      () -> -m_driverController.getLeftY(),
      () -> -m_driverController.getLeftX(),
      () -> -m_driverController.getRightX(),
      () -> -m_driverController.getRightY()
    ));

    // Lock the wheels on toggle
    m_driverController.start().toggleOnTrue(
      m_swerve.run(()->{
        m_swerve.lock();
      })
    );
    */

    /* TEST COMMANDS. DO NOT ENABLE DURING COMPETITION */

    /*
    // TEST-Vision snapping command.
    // If there is no limelight, there will be an exception
    m_driverController.rightBumper().whileTrue(
      m_swerve.driveCommandAngularVelocity(
        () -> -m_driverController.getLeftY(),
        () -> -m_driverController.getLeftX(),
        () -> -m_limelight.getTargetRotation()/360,
        Constants.OperatorConstants.kFastModeSpeed
      )
    );

    // TEST-Vision. Changes the wrist angle to what it should be at the moment the button is held.
    // To update the wrist angle, let go and hold the button again
    m_driverController.rightBumper().whileTrue(
      m_arm.SetWristAngle(getLimelightWristAngle())
    );
    
    // TEST-Drives to and runs a path planner path
    m_driverController.a().onTrue(m_swerve.driveToPathThenFollowPath(PathPlannerPath.fromPathFile("PlayAmp")));
    */
  }

  // AutonomousCommand
  public Command getAutonomousCommand() {
    return m_autonChooser.getSelected();
  }
}