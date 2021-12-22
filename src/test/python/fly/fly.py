from panel import *


class Fly:

    def __init__(self):
        pygame.init()
        pygame.time.set_timer(EVENT_CREAT_ENEMY, CREATE_HZ)
        self.bg = pygame.display.set_mode(PANEL_RECT.size)

        self.panels = pygame.sprite.Group(FlyPanel(), FlyPanel(True))
        self.enemys = pygame.sprite.Group()
        self.me = MePanel()
        self.mes = pygame.sprite.Group(self.me)
        self.clock = pygame.time.Clock()

    def start(self):
        while True:
            self.clock.tick(FRAME_PER_SEC)
            self.event_handle()
            self.update()

    def update(self):
        self.panels.update()
        self.panels.draw(self.bg)
        self.enemys.update()
        self.enemys.draw(self.bg)
        self.mes.update()
        self.mes.draw(self.bg)
        self.me.bullets.update()
        self.me.bullets.draw(self.bg)
        pygame.sprite.groupcollide(self.me.bullets, self.enemys, True, True)
        hits = pygame.sprite.spritecollide(self.me, self.enemys, True)
        if len(hits) > 0:
            self.me.kill()
            Fly.shutdown()
        pygame.display.update()

    def event_handle(self):
        events = pygame.event.get()
        for event in events:
            if event.type == pygame.QUIT:
                Fly.shutdown()
            elif event.type == EVENT_CREAT_ENEMY:
                self.enemys.add(EnemyPanel())
            elif event.type == pygame.KEYDOWN and event.key == pygame.K_SPACE:
                self.me.fire()

        key_pressed = pygame.key.get_pressed()
        if key_pressed[pygame.K_RIGHT]:
            self.me.speed = 2
        elif key_pressed[pygame.K_LEFT]:
            self.me.speed = -2
        else:
            self.me.speed = 0

    @staticmethod
    def shutdown():
        print("game over")
        pygame.quit()
        exit()


if __name__ == "__main__":
    fly = Fly()
    fly.start()

