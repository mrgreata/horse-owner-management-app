// src/app/component/owner/owner-create/owner-create.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { OwnerService } from 'src/app/service/owner.service';
import { OwnerCreateDto } from 'src/app/dto/owner';

@Component({
  standalone: true,
  selector: 'app-owner-create',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './owner-create.component.html',
})
export class OwnerCreateComponent {
  model: OwnerCreateDto = { firstName: '', lastName: '', email: null };
  submitting = false;
  errorMsg = '';

  constructor(
    private ownerService: OwnerService,
    private router: Router,
    private toast: ToastrService // ✅ Toast für Feedback
  ) {}

  save(): void {
    this.submitting = true;
    this.errorMsg = '';

    this.ownerService.create(this.model).subscribe({
      next: _ => {
        this.toast.success('Owner successfully created'); // ✅ Erfolgsmeldung
        this.router.navigate(['/owners']);
      },
      error: err => {
        this.errorMsg = err?.error?.message ?? 'Create failed';
        this.toast.error(this.errorMsg, 'Error'); // ✅ Fehlermeldung
        this.submitting = false;
      },
    });
  }

  cancel(): void {
    this.router.navigate(['/owners']); // ✅ zurück zur Liste
  }
}
