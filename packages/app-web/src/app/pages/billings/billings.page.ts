import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { OrderService } from '../../services/order.service';
import { Order } from '../../types';
import { AppConfigService } from '../../services/app-config.service';
import {
  IonButton,
  IonCol,
  IonContent,
  IonItem,
  IonList,
  IonRow,
} from '@ionic/angular/standalone';
import { DatePipe } from '@angular/common';
import { dateTimeFormat } from '../../services/session.service';
import { ServerConfigService } from '../../services/server-config.service';

@Component({
  selector: 'app-billings-page',
  templateUrl: './billings.page.html',
  styleUrls: ['./billings.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent, IonRow, IonCol, IonList, IonItem, DatePipe, IonButton],
  standalone: true,
})
export class BillingsPage implements OnInit {
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly appConfig = inject(AppConfigService);
  private readonly orderService = inject(OrderService);
  private readonly serverConfig = inject(ServerConfigService);

  busy = false;
  orders: Order[] = [];
  fromNow = relativeTimeOrElse;

  async ngOnInit() {
    this.appConfig.setPageTitle('Billings');
    await this.fetchOrders();
  }

  private async fetchOrders() {
    const page = 0;
    const orders = await this.orderService.orders({
      cursor: {
        page,
      },
    });
    this.orders.push(...orders);
    this.changeRef.detectChanges();
  }

  protected readonly dateTimeFormat = dateTimeFormat;

  getPayLink(order: Order) {
    return this.serverConfig.apiUrl + `/checkout/${order.id}`;
  }
}
