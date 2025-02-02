import { Component, inject } from '@angular/core';
import { ModalController } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { chevronBackOutline } from 'ionicons/icons';
import { RepositoryFull } from '../../graphql/types';
import { ArrayElement } from '../../types';

type Plugin = ArrayElement<RepositoryFull['plugins']>;

export interface FlowModalComponentProps {
  plugins: Plugin[];
}

@Component({
  selector: 'app-flow-modal',
  templateUrl: './flow-modal.component.html',
  styleUrls: ['./flow-modal.component.scss'],
  standalone: false,
})
export class FlowModalComponent implements FlowModalComponentProps {
  private readonly modalCtrl = inject(ModalController);

  constructor() {
    addIcons({ chevronBackOutline });
  }

  plugins: Plugin[];

  closeModal() {
    return this.modalCtrl.dismiss();
  }
}
