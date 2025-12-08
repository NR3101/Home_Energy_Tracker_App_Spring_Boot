CREATE TABLE  IF NOT EXISTS `device` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255),
    `type` VARCHAR(255),
    `location` VARCHAR(255),
    `user_id` BIGINT NOT NULL,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
);