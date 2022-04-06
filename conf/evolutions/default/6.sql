-- !Ups

-- -----------------------------------------------------
-- Table `internship_project`.`reaction`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `internship_project`.`reaction` (
    `id` INT NOT NULL,
    `reaction` VARCHAR(45) NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB;

INSERT INTO `internship_project`.`reaction` (`id`, `reaction`) VALUES ('1', 'likes');
INSERT INTO `internship_project`.`reaction` (`id`, `reaction`) VALUES ('2', 'dislikes');

-- !Downs

DROP TABLE `internship_project`.`reaction`;