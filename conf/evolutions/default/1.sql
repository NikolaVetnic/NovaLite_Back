-- !Ups

-- -----------------------------------------------------
-- Table `internship_project`.`role`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `internship_project`.`role` (
    `id` INT NOT NULL,
    `role` VARCHAR(45) NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB;

INSERT INTO `internship_project`.`role` (`id`, `role`) VALUES ('1', 'admin');
INSERT INTO `internship_project`.`role` (`id`, `role`) VALUES ('2', 'user');

-- !Downs

DROP TABLE `internship_project`.`role`;