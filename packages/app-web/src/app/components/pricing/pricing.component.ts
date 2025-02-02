import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  input,
  OnInit,
  output,
} from '@angular/core';
import { filter, isNull } from 'lodash-es';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { Feature, FeatureGroup, Product } from '../../graphql/types';
import {
  GqlFeatureName,
  GqlPricedProduct,
  GqlRecurringPaymentInterval,
  GqlVertical,
} from '../../../generated/graphql';
import { FeatureService } from '../../services/feature.service';
import {
  AlertController,
  IonButton,
  IonCol,
  IonLabel,
  IonRow,
  IonSegment,
  IonSegmentButton,
  IonText,
  ToastController,
} from '@ionic/angular/standalone';
import { Plan, PlanService } from '../../services/plan.service';
import { RemoveIfProdDirective } from '../../directives/remove-if-prod/remove-if-prod.directive';
import { FeatureComponent } from '../feature/feature.component';
import { ActivatedRoute } from '@angular/router';

export type StringFeatureGroup = {
  groupLabel: string;
  features: Feature[];
};

type TargetGroup = 'organization' | 'individual' | 'other';
type ProductFlavor = 'selfHosting' | 'saas';
type PaymentInterval = GqlRecurringPaymentInterval;

type Price = Pick<
  GqlPricedProduct,
  'id' | 'recurringInterval' | 'description' | 'inStock' | 'price'
>;

type ProductWithFeatureGroups = Product & {
  stringifiedFeatureGroups: StringFeatureGroup[];
  featureGroups: FeatureGroup[];
};

@Component({
  selector: 'app-pricing',
  templateUrl: './pricing.component.html',
  styleUrls: ['./pricing.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonSegment,
    FormsModule,
    ReactiveFormsModule,
    IonSegmentButton,
    IonLabel,
    IonButton,
    RemoveIfProdDirective,
    IonText,
    FeatureComponent,
    IonRow,
    IonCol,
  ],
  standalone: true,
})
export class PricingComponent implements OnInit {
  private readonly featureService = inject(FeatureService);
  private readonly productService = inject(ProductService);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly planService = inject(PlanService);
  private readonly toastCtrl = inject(ToastController);
  private readonly alertCtrl = inject(AlertController);
  private readonly activatedRoute = inject(ActivatedRoute);

  targetGroupFc = new FormControl<TargetGroup>('individual');
  paymentIntervalFc = new FormControl<PaymentInterval>(
    GqlRecurringPaymentInterval.Yearly,
  );
  productFlavorFc = new FormControl<ProductFlavor>('saas');
  productFlavorSelf: ProductFlavor = 'selfHosting';
  productFlavorCloud: ProductFlavor = 'saas';
  targetGroupOrganization: TargetGroup = 'organization';
  targetGroupIndividual: TargetGroup = 'individual';
  targetGroupOther: TargetGroup = 'other';
  paymentIntervalMonthly: PaymentInterval = GqlRecurringPaymentInterval.Monthly;
  paymentIntervalYearly: PaymentInterval = GqlRecurringPaymentInterval.Yearly;
  private products: ProductWithFeatureGroups[];

  readonly vertical = input.required<GqlVertical>();

  readonly serviceFlavor = input<ProductFlavor>();

  readonly hideServiceFlavor = input<boolean>();

  readonly selectionChange = output<Product>();
  protected subscribedPlans: Plan[] = [];

  async ngOnInit() {
    const serviceFlavor = this.serviceFlavor();
    if (serviceFlavor) {
      this.productFlavorFc.setValue(serviceFlavor);
    } else {
      this.productFlavorFc.setValue(
        [
          this.activatedRoute.snapshot.queryParams.flavor,
          this.productFlavorSelf,
        ].filter((flavor) =>
          [this.productFlavorSelf, this.productFlavorCloud].includes(flavor),
        )[0],
      );
    }
    const products = await this.productService.listProducts({
      vertical: this.vertical(),
    });

    this.subscribedPlans = await this.planService.fetchPlans({ page: 0 });

    this.products = await Promise.all(
      products.map<Promise<ProductWithFeatureGroups>>(async (p) => {
        console.log('p.featureGroupId', p.featureGroupId);
        const featureGroups = p.featureGroupId
          ? await this.featureService.findAll(
              { id: { eq: p.featureGroupId } },
              true,
            )
          : [];
        return {
          ...p,
          stringifiedFeatureGroups:
            featureGroups.length > 0
              ? [await this.stringifyFeatureGroup(featureGroups)]
              : [],
          featureGroups: featureGroups.length > 0 ? featureGroups : [],
        };
      }),
    );
    this.changeRef.detectChanges();
  }

  filteredProducts(): ProductWithFeatureGroups[] {
    if (!this.products) {
      return [];
    }
    return filter<ProductWithFeatureGroups>(this.products, this.filterParams());
  }

  private filterParams() {
    if (this.productFlavorFc.value === 'saas') {
      return {
        isCloud: true,
      };
    }
    if (this.targetGroupFc.value === 'individual') {
      return {
        individual: true,
      };
    }
    if (this.targetGroupFc.value === 'organization') {
      return {
        enterprise: true,
      };
    }
    if (this.targetGroupFc.value === 'other') {
      return {
        other: true,
      };
    }
  }

  filteredPrices(prices: GqlPricedProduct[]): GqlPricedProduct[] {
    return filter<GqlPricedProduct>(prices)
      .filter((price) => price.price >= 0)
      .filter(
        (price) => price.recurringInterval === this.paymentIntervalFc.value,
      );
  }

  private async stringifyFeatureGroup(
    featureGroups: FeatureGroup[],
  ): Promise<StringFeatureGroup> {
    return {
      groupLabel: 'Features',
      features: featureGroups[0].features,
    };
  }

  buySubscription(product: Product) {
    // this.products.find(p => this.hasSubscribed(p));
    this.selectionChange.emit(product);
  }

  getProductActionLabel(product: ProductWithFeatureGroups) {
    if (product.isCloud) {
      const features = product.featureGroups.flatMap((fg) => fg.features);
      const canActivate = features
        .filter((feature) => feature.name === GqlFeatureName.CanActivatePlan)
        .some((feature) => feature.value.boolVal.value === true);
      if (canActivate) {
        return 'Subscribe';
      } else {
        return 'Notify me';
      }
    } else {
      return 'Buy';
    }
  }

  formatPrice(price: number) {
    return price.toFixed(2);
  }

  hasSubscribed(product: ProductWithFeatureGroups): boolean {
    return this.subscribedPlans.some(
      (subscribedPlan) =>
        subscribedPlan.productId === product.id &&
        isNull(subscribedPlan.terminatedAt),
    );
  }

  async cancelSubscription(product: ProductWithFeatureGroups) {
    const plan = this.subscribedPlans.find(
      (subscribedPlan) =>
        subscribedPlan.productId === product.id &&
        isNull(subscribedPlan.terminatedAt),
    );

    if (plan) {
      const alert = await this.alertCtrl.create({
        header: 'Cancel Subscription?',
        message: `You will be downgraded to a free plan, if available.`,
        // cssClass: 'fatal-alert',
        buttons: [
          {
            text: 'No, keep it unchanged',
            role: 'cancel',
          },
          {
            text: 'Yes, Confirm',
            role: 'confirm',
            cssClass: 'confirm-button',
            handler: async () => {
              await this.planService.cancelPlanSubscription(plan.id);

              const toast = await this.toastCtrl.create({
                message: 'Plan cancelled',
                duration: 3000,
                color: 'success',
              });

              await toast.present();
            },
          },
        ],
      });
      await alert.present();
    }
  }

  getDiscount(product: ProductWithFeatureGroups, price: Price) {
    const basePrice = this.filteredPrices(product.prices)[0];
    return (100*(1 - price.price / basePrice.price)).toFixed(0);
  }
}
