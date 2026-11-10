using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers
{
    public class InteracoesController : Controller
    {
        private readonly MiniCrmContext _context;

        public InteracoesController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: Interacoes
        public async Task<IActionResult> Index()
        {
            var miniCrmContext = _context.Interacoes.Include(i => i.IdClienteNavigation).Include(i => i.IdUsuarioNavigation);
            return View(await miniCrmContext.ToListAsync());
        }

        // GET: Interacoes/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var interacao = await _context.Interacoes
                .Include(i => i.IdClienteNavigation)
                .Include(i => i.IdUsuarioNavigation)
                .FirstOrDefaultAsync(m => m.IdInteracao == id);
            if (interacao == null)
            {
                return NotFound();
            }

            return View(interacao);
        }

        // GET: Interacoes/Create
        public IActionResult Create()
        {
            ViewData["IdCliente"] = new SelectList(_context.Clientes, "IdCliente", "Email");
            ViewData["IdUsuario"] = new SelectList(_context.Usuarios, "IdUsuario", "Email");
            return View();
        }

        // POST: Interacoes/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("IdInteracao,CriadoEm,DataInteracao,Observacoes,Tipo,IdCliente,IdUsuario")] Interacao interacao)
        {
            if (ModelState.IsValid)
            {
                _context.Add(interacao);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            ViewData["IdCliente"] = new SelectList(_context.Clientes, "IdCliente", "Email", interacao.IdCliente);
            ViewData["IdUsuario"] = new SelectList(_context.Usuarios, "IdUsuario", "Email", interacao.IdUsuario);
            return View(interacao);
        }

        // GET: Interacoes/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var interacao = await _context.Interacoes.FindAsync(id);
            if (interacao == null)
            {
                return NotFound();
            }
            ViewData["IdCliente"] = new SelectList(_context.Clientes, "IdCliente", "Email", interacao.IdCliente);
            ViewData["IdUsuario"] = new SelectList(_context.Usuarios, "IdUsuario", "Email", interacao.IdUsuario);
            return View(interacao);
        }

        // POST: Interacoes/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("IdInteracao,CriadoEm,DataInteracao,Observacoes,Tipo,IdCliente,IdUsuario")] Interacao interacao)
        {
            if (id != interacao.IdInteracao)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(interacao);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!InteracaoExists(interacao.IdInteracao))
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            ViewData["IdCliente"] = new SelectList(_context.Clientes, "IdCliente", "Email", interacao.IdCliente);
            ViewData["IdUsuario"] = new SelectList(_context.Usuarios, "IdUsuario", "Email", interacao.IdUsuario);
            return View(interacao);
        }

        // GET: Interacoes/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var interacao = await _context.Interacoes
                .Include(i => i.IdClienteNavigation)
                .Include(i => i.IdUsuarioNavigation)
                .FirstOrDefaultAsync(m => m.IdInteracao == id);
            if (interacao == null)
            {
                return NotFound();
            }

            return View(interacao);
        }

        // POST: Interacoes/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var interacao = await _context.Interacoes.FindAsync(id);
            if (interacao != null)
            {
                _context.Interacoes.Remove(interacao);
            }

            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool InteracaoExists(int id)
        {
            return _context.Interacoes.Any(e => e.IdInteracao == id);
        }
    }
}
