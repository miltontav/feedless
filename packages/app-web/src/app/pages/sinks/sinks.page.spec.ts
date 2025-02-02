import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SinksPage } from './sinks.page';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../app-test.module';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('SinksPage', () => {
  let component: SinksPage;
  let fixture: ComponentFixture<SinksPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SinksPage, AppTestModule.withDefaults()],
      providers: [],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(SinksPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
