// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.TestMotor;


public class PlaySpeakerCommand extends SequentialCommandGroup {
  public PlaySpeakerCommand(TestMotor testMotor, double testMotorSpeed) {
    // Add your commands in the addCommands() call, e.g.
    // addCommands(new FooCommand(), new BarCommand());
    addCommands(
      Commands.startEnd(
      () -> { testMotor.setMotor(testMotorSpeed); }, 
      () -> { testMotor.setMotor(0); }, 
      testMotor));
  }
}