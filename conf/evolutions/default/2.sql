-- !Ups

-- -----------------------------------------------------
-- Table `internship_project`.`user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `internship_project`.`user` (
    `id` INT NOT NULL,
    `username` VARCHAR(100) NULL,
    `first_name` VARCHAR(45) NULL,
    `last_name` VARCHAR(45) NULL,
    `password` VARCHAR(45) NULL,
    `img_url` VARCHAR(100) NULL,
    `role_id` INT NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `fk_user_role_idx` (`role_id` ASC) VISIBLE,
    CONSTRAINT `fk_user_role`
    FOREIGN KEY (`role_id`)
    REFERENCES `internship_project`.`role` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB;

INSERT INTO `internship_project`.`user` (`id`, `username`, `first_name`, `last_name`, `password`, `role_id`, `img_url`) VALUES ('1', 'nikola.pacek.vetnic@gmail.com', 'Nikola', 'Vetnic', 'adMIN1234!', '1', 'https://riverlegacy.org/wp-content/uploads/2021/07/blank-profile-photo.jpeg');
INSERT INTO `internship_project`.`user` (`id`, `username`, `first_name`, `last_name`, `password`, `role_id`, `img_url`) VALUES ('2', 'alice.alison@gmail.com', 'Alice', 'Alison', 'adMIN1234!', '2', 'https://riverlegacy.org/wp-content/uploads/2021/07/blank-profile-photo.jpeg');
INSERT INTO `internship_project`.`user` (`id`, `username`, `first_name`, `last_name`, `password`, `role_id`, `img_url`) VALUES ('3', 'bob.robertson@gmail.com', 'Bob', 'Robertson', 'adMIN1234!', '2', 'https://riverlegacy.org/wp-content/uploads/2021/07/blank-profile-photo.jpeg');

-- !Downs

DROP TABLE `internship_project`.`user`;