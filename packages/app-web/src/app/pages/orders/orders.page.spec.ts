import { ComponentFixture, TestBed } from '@angular/core/testing';
import { OrdersPage } from './orders.page';
import {
  AppTestModule,
  mockBillings,
  mockRepositories,
} from '../../app-test.module';

describe('OrdersPage', () => {
  let component: OrdersPage;
  let fixture: ComponentFixture<OrdersPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        OrdersPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockRepositories(apolloMockController);
            mockBillings(apolloMockController);
          },
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(OrdersPage);
    component = fixture.componentInstance;
    component.orders = [];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
