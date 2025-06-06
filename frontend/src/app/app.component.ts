import {Component} from '@angular/core';
import {HeaderComponent} from './header/header.component';
import {AuthComponent} from "./auth/auth.component";
import {ContentComponent} from "./content/content.component";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-root',
  imports: [HeaderComponent, ContentComponent, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontend';
}
