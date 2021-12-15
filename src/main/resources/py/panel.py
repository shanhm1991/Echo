import pygame
import random

FRAME_PER_SEC = 60
CREATE_HZ = 500
PANEL_RECT = pygame.Rect(0, 0, 480, 700)
EVENT_CREAT_ENEMY = pygame.USEREVENT
EVENT_ME_FIRE = pygame.USEREVENT + 1


class Panel(pygame.sprite.Sprite):

    def __init__(self, _image, speed=1):
        super().__init__()
        self.image = pygame.image.load(_image)
        self.rect = self.image.get_rect()
        self.speed = speed

    def update(self):
        self.rect.y += self.speed


class FlyPanel(Panel):

    def __init__(self, is_alt=False):
        super().__init__("./images/bg.png")
        if is_alt:
            self.rect.y = -self.rect.height

    def update(self):
        super().update()
        if self.rect.y >= 700:
            self.rect.y = -700


class EnemyPanel(Panel):

    def __init__(self):
        super().__init__("./images/enemy1.png")
        self.speed = random.randint(2, 4)
        self.rect.bottom = 0
        self.rect.x = random.randint(0, PANEL_RECT.width - self.rect.width)

    def __del__(self):
        pass

    def update(self):
        super().update()
        if self.rect.y >= PANEL_RECT.height:
            self.kill()


class MePanel(Panel):

    def __init__(self):
        super().__init__("./images/me1.png", 0)
        self.rect.centerx = PANEL_RECT.centerx
        self.rect.bottom = PANEL_RECT.bottom - 35
        self.bullets = pygame.sprite.Group()

    def update(self):
        self.rect.x += self.speed
        if self.rect.x < 0:
            self.rect.x = 0
        if self.rect.x > PANEL_RECT.right:
            self.rect.x = PANEL_RECT.right

    def fire(self):
        bullet = BulletPanel()
        bullet.rect.bottom = self.rect.y - 10
        bullet.rect.centerx = self.rect.centerx
        self.bullets.add(bullet)


class BulletPanel(Panel):

    def __init__(self):
        super().__init__("./images/bullet1.png", -2)

    def __del__(self):
        pass

    def update(self):
        super().update()
        if self.rect.bottom < 0:
            self.kill()
