import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import {
  AppConfigService,
  VerticalSpecWithRoutes,
} from '../../services/app-config.service';
import { IonContent } from '@ionic/angular/standalone';
import { NotebooksComponent } from '../../components/notebooks/notebooks.component';

@Component({
  selector: 'app-notebook-page',
  templateUrl: './notebooks.page.html',
  styleUrls: ['./notebooks.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent, NotebooksComponent],
  standalone: true,
})
export class NotebooksPage implements OnInit, OnDestroy {
  private readonly appConfigService = inject(AppConfigService);
  private readonly changeRef = inject(ChangeDetectorRef);

  loading = false;
  productConfig: VerticalSpecWithRoutes;
  private subscriptions: Subscription[] = [];

  async ngOnInit() {
    this.appConfigService.setPageTitle('Notebooks');
    this.subscriptions.push(
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
